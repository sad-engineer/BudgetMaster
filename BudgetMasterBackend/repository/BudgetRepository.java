package repository;

import model.Budget;
import util.DateTimeUtil;
import java.sql.*;
import java.util.*;

public class BudgetRepository extends BaseRepository implements Repository<Budget, Integer> {

    public BudgetRepository(String dbPath) {
        super(dbPath);
    }

    @Override
    public Budget save(Budget budget) {
        // Automatically set position if not set (equals 0)
        setAutoPosition(budget, "budgets");
        
        String sql = "INSERT INTO budgets (create_time, update_time, delete_time, created_by, updated_by, deleted_by, position, amount, currency_id, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Форматируем даты в совместимом с SQLite формате
            String createTimeStr = DateTimeUtil.formatForSqlite(budget.getCreateTime());
            String updateTimeStr = DateTimeUtil.formatForSqlite(budget.getUpdateTime());
            String deleteTimeStr = DateTimeUtil.formatForSqlite(budget.getDeleteTime());
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, budget.getCreatedBy());
            stmt.setString(5, budget.getUpdatedBy());
            stmt.setString(6, budget.getDeletedBy());
            stmt.setInt(7, budget.getPosition());
            stmt.setInt(8, budget.getAmount());
            stmt.setInt(9, budget.getCurrencyId());
            if (budget.getCategoryId() != null) stmt.setInt(10, budget.getCategoryId()); else stmt.setNull(10, Types.INTEGER);
            stmt.executeUpdate();
            
            // Получаем сгенерированный id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    budget.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Normalize positions after save
        normalizePositions("budgets");
        
        return budget;
    }

    @Override
    public Optional<Budget> findById(Integer id) {
        return findById("budgets", id, this::mapRowSafe);
    }

    @Override
    public List<Budget> findAll() {
        return findAll("budgets", this::mapRowSafe);
    }

    @Override
    public Budget update(Budget budget) {
        // Adjust positions if needed before updating
        adjustPositionsForUpdate(budget, "budgets", budget.getId());
        
        String sql = "UPDATE budgets SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, position=?, amount=?, currency_id=?, category_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Форматируем даты в совместимом с SQLite формате
            String createTimeStr = DateTimeUtil.formatForSqlite(budget.getCreateTime());
            String updateTimeStr = DateTimeUtil.formatForSqlite(budget.getUpdateTime());
            String deleteTimeStr = DateTimeUtil.formatForSqlite(budget.getDeleteTime());
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, budget.getCreatedBy());
            stmt.setString(5, budget.getUpdatedBy());
            stmt.setString(6, budget.getDeletedBy());
            stmt.setInt(7, budget.getPosition());
            stmt.setInt(8, budget.getAmount());
            stmt.setInt(9, budget.getCurrencyId());
            if (budget.getCategoryId() != null) stmt.setInt(10, budget.getCategoryId()); else stmt.setNull(10, Types.INTEGER);
            stmt.setInt(11, budget.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Normalize positions after update
        normalizePositions("budgets");
        
        return budget;
    }

    @Override
    public boolean delete(Integer id) {
        return softDelete("budgets", id, "system");
    }

    /**
     * Soft delete with custom deletedBy parameter
     * @param id entity ID
     * @param deletedBy user who deleted the entity
     * @return true if successful
     */
    public boolean delete(Integer id, String deletedBy) {
        return softDelete("budgets", id, deletedBy);
    }

    /**
     * Restores a soft-deleted budget
     * @param id budget ID
     * @return true if successful
     */
    public boolean restore(Integer id) {
        return restore("budgets", id);
    }

    /**
     * Gets only deleted budgets
     * @return list of deleted budgets
     */
    public List<Budget> findDeleted() {
        return findDeleted("budgets", this::mapRowSafe);
    }

    /**
     * Normalizes positions of all active budgets to be sequential starting from 1
     */
    public void normalizePositions() {
        normalizePositions("budgets");
    }

    private Budget mapRow(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setId(rs.getInt("id"));
        
        // Читаем даты как строки и парсим их
        String createTimeStr = rs.getString("create_time");
        budget.setCreateTime(DateTimeUtil.parseFromSqlite(createTimeStr));
        
        String updateTimeStr = rs.getString("update_time");
        budget.setUpdateTime(DateTimeUtil.parseFromSqlite(updateTimeStr));
        
        String deleteTimeStr = rs.getString("delete_time");
        budget.setDeleteTime(DateTimeUtil.parseFromSqlite(deleteTimeStr));
        
        budget.setCreatedBy(rs.getString("created_by"));
        budget.setUpdatedBy(rs.getString("updated_by"));
        budget.setDeletedBy(rs.getString("deleted_by"));
        budget.setPosition(rs.getInt("position"));
        budget.setAmount(rs.getInt("amount"));
        budget.setCurrencyId(rs.getInt("currency_id"));
        int val = rs.getInt("category_id");
        budget.setCategoryId(rs.wasNull() ? null : val);
        return budget;
    }

    private Budget mapRowSafe(ResultSet rs) {
        try {
            return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
} 