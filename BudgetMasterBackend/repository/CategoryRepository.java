package repository;

import model.Category;
import util.DateTimeUtil;
import java.sql.*;
import java.util.*;

public class CategoryRepository extends BaseRepository implements Repository<Category, Integer> {

    public CategoryRepository(String dbPath) {
        super(dbPath);
    }

    @Override
    public Category save(Category category) {
        // Проверяем, есть ли удаленная запись с таким же заголовком, и восстанавливаем ее
        if (checkAndRestoreDeletedRecord(category, "categories", "title", category.getTitle())) {
            return category; // Запись восстановлена
        }
        
        // Автоматически устанавливаем позицию, если она не установлена (равна 0)
        setAutoPosition(category, "categories");
        
        String sql = "INSERT INTO categories (create_time, update_time, delete_time, created_by, updated_by, deleted_by, position, title, operation_type, type, parent_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Форматируем даты в совместимом с SQLite формате
            String createTimeStr = DateTimeUtil.formatForSqlite(category.getCreateTime());
            String updateTimeStr = DateTimeUtil.formatForSqlite(category.getUpdateTime());
            String deleteTimeStr = DateTimeUtil.formatForSqlite(category.getDeleteTime());
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, category.getCreatedBy());
            stmt.setString(5, category.getUpdatedBy());
            stmt.setString(6, category.getDeletedBy());
            stmt.setInt(7, category.getPosition());
            stmt.setString(8, category.getTitle());
            stmt.setInt(9, category.getOperationType());
            stmt.setInt(10, category.getType());
            if (category.getParentId() != null) stmt.setInt(11, category.getParentId()); else stmt.setNull(11, Types.INTEGER);
            stmt.executeUpdate();
            
            // Получаем сгенерированный id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    category.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Нормализуем позиции после сохранения
        normalizePositions("categories");
        
        return category;
    }

    @Override
    public Optional<Category> findById(Integer id) {
        return findById("categories", id, this::mapRowSafe);
    }

    @Override
    public List<Category> findAll() {
        return findAll("categories", this::mapRowSafe);
    }

    @Override
    public Category update(Category category) {
        // Корректируем позиции, если они нужны перед обновлением
        adjustPositionsForUpdate(category, "categories", category.getId());
        
        String sql = "UPDATE categories SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, position=?, title=?, operation_type=?, type=?, parent_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Форматируем даты в совместимом с SQLite формате
            String createTimeStr = DateTimeUtil.formatForSqlite(category.getCreateTime());
            String updateTimeStr = DateTimeUtil.formatForSqlite(category.getUpdateTime());
            String deleteTimeStr = DateTimeUtil.formatForSqlite(category.getDeleteTime());
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, category.getCreatedBy());
            stmt.setString(5, category.getUpdatedBy());
            stmt.setString(6, category.getDeletedBy());
            stmt.setInt(7, category.getPosition());
            stmt.setString(8, category.getTitle());
            stmt.setInt(9, category.getOperationType());
            stmt.setInt(10, category.getType());
            if (category.getParentId() != null) stmt.setInt(11, category.getParentId()); else stmt.setNull(11, Types.INTEGER);
            stmt.setInt(12, category.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Нормализуем позиции после обновления
        normalizePositions("categories");
        
        return category;
    }

    @Override
    public boolean delete(Integer id) {
        return softDelete("categories", id, "system");
    }

    /**
     * Soft delete с пользовательским параметром deletedBy
     * @param id ID сущности
     * @param deletedBy пользователь, который удалил сущность
     */
    public boolean delete(Integer id, String deletedBy) {
        return softDelete("categories", id, deletedBy);
    }

    /**
     * Восстанавливает удаленную категорию
     * @param id ID категории
     * @return true, если успешно
     */
    public boolean restore(Integer id) {
        return restore("categories", id);
    }

    /**
     * Получает только удаленные категории
     * @return список удаленных категорий
     */
    public List<Category> findDeleted() {
        return findDeleted("categories", this::mapRowSafe);
    }

    /**
     * Нормализует позиции всех активных категорий, чтобы они были последовательными, начиная с 1
     */
    public void normalizePositions() {
        normalizePositions("categories");
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        
        // Читаем даты как строки и парсим их
        String createTimeStr = rs.getString("create_time");
        category.setCreateTime(DateTimeUtil.parseFromSqlite(createTimeStr));
        
        String updateTimeStr = rs.getString("update_time");
        category.setUpdateTime(DateTimeUtil.parseFromSqlite(updateTimeStr));
        
        String deleteTimeStr = rs.getString("delete_time");
        category.setDeleteTime(DateTimeUtil.parseFromSqlite(deleteTimeStr));
        
        category.setCreatedBy(rs.getString("created_by"));
        category.setUpdatedBy(rs.getString("updated_by"));
        category.setDeletedBy(rs.getString("deleted_by"));
        category.setPosition(rs.getInt("position"));
        category.setTitle(rs.getString("title"));
        category.setOperationType(rs.getInt("operation_type"));
        category.setType(rs.getInt("type"));
        int val = rs.getInt("parent_id");
        category.setParentId(rs.wasNull() ? null : val);
        return category;
    }

    private Category mapRowSafe(ResultSet rs) {
        try {
            return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
} 