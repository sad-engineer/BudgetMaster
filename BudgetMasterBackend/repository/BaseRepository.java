package repository;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import util.DateTimeUtil;

/**
 * Базовый класс для репозиториев с поддержкой автонумерации position
 */
public abstract class BaseRepository {
    protected final String url;

    public BaseRepository(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
    }

    protected Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        // Set UTF-8 encoding for connection
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA encoding = 'UTF-8'");
        }
        return conn;
    }

    /**
     * Получает следующий доступный position для указанной таблицы
     * @param tableName имя таблицы
     * @return следующий доступный position
     */
    protected int getNextPosition(String tableName) {
        String sql = "SELECT COALESCE(MAX(position), 0) + 1 FROM " + tableName;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // fallback
    }

    /**
     * Automatically sets position for entity if it's not set (equals 0)
     * @param entity entity with position field
     * @param tableName table name
     */
    protected void setAutoPosition(Object entity, String tableName) {
        try {
            // Use reflection to get and set position
            java.lang.reflect.Method getPosition = entity.getClass().getMethod("getPosition");
            java.lang.reflect.Method setPosition = entity.getClass().getMethod("setPosition", int.class);
            
            int currentPosition = (Integer) getPosition.invoke(entity);
            if (currentPosition == 0) {
                int nextPosition = getNextPosition(tableName);
                setPosition.invoke(entity, nextPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Adjusts positions when updating an entity to avoid conflicts
     * @param entity entity being updated
     * @param tableName table name
     * @param entityId entity ID (to exclude from position adjustment)
     */
    protected void adjustPositionsForUpdate(Object entity, String tableName, Integer entityId) {
        try {
            java.lang.reflect.Method getPosition = entity.getClass().getMethod("getPosition");
            java.lang.reflect.Method getId = entity.getClass().getMethod("getId");
            
            int newPosition = (Integer) getPosition.invoke(entity);
            int currentId = (Integer) getId.invoke(entity);
            
            // Get current position of the entity being updated
            String currentPositionSql = "SELECT position FROM " + tableName + " WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement currentStmt = conn.prepareStatement(currentPositionSql)) {
                currentStmt.setInt(1, currentId);
                ResultSet currentRs = currentStmt.executeQuery();
                if (currentRs.next()) {
                    int currentPosition = currentRs.getInt(1);
                    
                    // If position is not changing, no need to adjust
                    if (currentPosition == newPosition) {
                        return;
                    }
                    
                    // Check if new position is already taken by another entity
                    String checkSql = "SELECT COUNT(*) FROM " + tableName + " WHERE position = ? AND id != ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                        checkStmt.setInt(1, newPosition);
                        checkStmt.setInt(2, currentId);
                        ResultSet checkRs = checkStmt.executeQuery();
                        if (checkRs.next() && checkRs.getInt(1) > 0) {
                            // Position is taken, we need to shift positions
                            if (currentPosition < newPosition) {
                                // Moving to higher position: shift positions between current and new down by 1
                                String shiftSql = "UPDATE " + tableName + " SET position = position - 1 WHERE position > ? AND position <= ? AND id != ?";
                                try (PreparedStatement shiftStmt = conn.prepareStatement(shiftSql)) {
                                    shiftStmt.setInt(1, currentPosition);
                                    shiftStmt.setInt(2, newPosition);
                                    shiftStmt.setInt(3, currentId);
                                    shiftStmt.executeUpdate();
                                }
                            } else {
                                // Moving to lower position: shift positions between new and current up by 1
                                String shiftSql = "UPDATE " + tableName + " SET position = position + 1 WHERE position >= ? AND position < ? AND id != ?";
                                try (PreparedStatement shiftStmt = conn.prepareStatement(shiftSql)) {
                                    shiftStmt.setInt(1, newPosition);
                                    shiftStmt.setInt(2, currentPosition);
                                    shiftStmt.setInt(3, currentId);
                                    shiftStmt.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs soft delete by setting delete_time and deleted_by fields
     * @param tableName table name
     * @param id entity ID
     * @param deletedBy user who deleted the entity
     * @return true if successful
     */
    protected boolean softDelete(String tableName, Integer id, String deletedBy) {
        String sql = "UPDATE " + tableName + " SET delete_time = ?, deleted_by = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Use DateTimeUtil to format the date properly for SQLite
            String deleteTime = util.DateTimeUtil.formatForSqlite(java.time.LocalDateTime.now());
            stmt.setString(1, deleteTime);
            stmt.setString(2, deletedBy);
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Flag to control whether to include deleted entities in findAll results
     */
    protected boolean includeDeleted = false;

    /**
     * Sets whether to include deleted entities in findAll results
     * @param includeDeleted true to include deleted entities, false to exclude them
     */
    public void setIncludeDeleted(boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }

    /**
     * Gets the WHERE clause for filtering deleted entities
     * @return WHERE clause string
     */
    protected String getDeletedFilterClause() {
        if (includeDeleted) {
            return ""; // No filter, include all
        } else {
            return " WHERE delete_time IS NULL";
        }
    }

    /**
     * Gets the AND clause for filtering deleted entities (for use with existing WHERE)
     * @return AND clause string
     */
    protected String getDeletedFilterAndClause() {
        if (includeDeleted) {
            return ""; // No filter, include all
        } else {
            return " AND delete_time IS NULL";
        }
    }

    /**
     * Restores a soft-deleted entity by clearing delete_time and deleted_by
     * @param tableName table name
     * @param id entity ID
     * @return true if successful
     */
    protected boolean restore(String tableName, Integer id) {
        String sql = "UPDATE " + tableName + " SET delete_time = NULL, deleted_by = NULL WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            
            // Normalize positions after restore
            if (result) {
                normalizePositions(tableName);
            }
            
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Normalizes positions to be sequential starting from 1
     * @param tableName table name
     */
    protected void normalizePositions(String tableName) {
        try (Connection conn = getConnection()) {
            // Get all records ordered by current position
            String selectSql = "SELECT id, position FROM " + tableName + " WHERE delete_time IS NULL ORDER BY position ASC";
            List<Integer> ids = new ArrayList<>();
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                while (rs.next()) {
                    ids.add(rs.getInt("id"));
                }
            }
            
            // Update positions sequentially starting from 1
            if (!ids.isEmpty()) {
                String updateSql = "UPDATE " + tableName + " SET position = ? WHERE id = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    for (int i = 0; i < ids.size(); i++) {
                        updateStmt.setInt(1, i + 1); // Position starts from 1
                        updateStmt.setInt(2, ids.get(i));
                        updateStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if positions need normalization and performs it if needed
     * @param tableName table name
     */
    protected void checkAndNormalizePositions(String tableName) {
        try (Connection conn = getConnection()) {
            // Check if there are gaps in positions
            String checkSql = "SELECT COUNT(*) as total, MAX(position) as max_pos FROM " + tableName + " WHERE delete_time IS NULL";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int maxPos = rs.getInt("max_pos");
                    
                    // If max position is greater than total count, there are gaps
                    if (maxPos > total) {
                        normalizePositions(tableName);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generic method to check and restore deleted record with same title/name
     * @param entity entity to save
     * @param tableName table name
     * @param titleField field name for title/name (e.g., "title", "name")
     * @param titleValue value of the title field
     * @return true if restored, false if not found
     */
    protected boolean checkAndRestoreDeletedRecord(Object entity, String tableName, String titleField, String titleValue) {
        String checkSql = "SELECT id FROM " + tableName + " WHERE " + titleField + " = ? AND delete_time IS NOT NULL";
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, titleValue);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                // Found a deleted record with the same title, restore it
                int existingId = rs.getInt("id");
                
                // Get the maximum position and add 1 for the restored record
                int newPosition = getNextPosition(tableName);
                
                String restoreSql = "UPDATE " + tableName + " SET delete_time = NULL, deleted_by = NULL, update_time = ?, updated_by = ?, position = ? WHERE id = ?";
                try (PreparedStatement restoreStmt = conn.prepareStatement(restoreSql)) {
                    // Use reflection to get update time and updated by
                    java.lang.reflect.Method getUpdateTime = entity.getClass().getMethod("getUpdateTime");
                    java.lang.reflect.Method getUpdatedBy = entity.getClass().getMethod("getUpdatedBy");
                    java.lang.reflect.Method setId = entity.getClass().getMethod("setId", int.class);
                    java.lang.reflect.Method setPosition = entity.getClass().getMethod("setPosition", int.class);
                    
                    Object updateTime = getUpdateTime.invoke(entity);
                    String updatedBy = (String) getUpdatedBy.invoke(entity);
                    
                    String updateTimeStr = DateTimeUtil.formatForSqlite((java.time.LocalDateTime) updateTime);
                    restoreStmt.setString(1, updateTimeStr);
                    restoreStmt.setString(2, updatedBy);
                    restoreStmt.setInt(3, newPosition);
                    restoreStmt.setInt(4, existingId);
                    restoreStmt.executeUpdate();
                    
                    // Set the ID and position to the restored record
                    setId.invoke(entity, existingId);
                    setPosition.invoke(entity, newPosition);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Generic method to find deleted entities
     * @param tableName table name
     * @param mapper function to map ResultSet to entity
     * @return list of deleted entities
     */
    protected <T> List<T> findDeleted(String tableName, java.util.function.Function<ResultSet, T> mapper) {
        List<T> result = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName + " WHERE delete_time IS NOT NULL ORDER BY position ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapper.apply(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Generic method to find entity by ID
     * @param tableName table name
     * @param id entity ID
     * @param mapper function to map ResultSet to entity
     * @return Optional of entity
     */
    protected <T> Optional<T> findById(String tableName, Integer id, java.util.function.Function<ResultSet, T> mapper) {
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?" + getDeletedFilterAndClause();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapper.apply(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Generic method to find all entities
     * @param tableName table name
     * @param mapper function to map ResultSet to entity
     * @return list of entities
     */
    protected <T> List<T> findAll(String tableName, java.util.function.Function<ResultSet, T> mapper) {
        List<T> result = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName + getDeletedFilterClause() + " ORDER BY position ASC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapper.apply(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
} 