// -*- coding: utf-8 -*-
package repository;

import model.Currency;
import util.DateTimeUtil;
import java.sql.*;
import java.util.*;

public class CurrencyRepository extends BaseRepository implements Repository<Currency, Integer> {

    public CurrencyRepository(String dbPath) {
        super(dbPath);
    }

    @Override
    public Currency save(Currency currency) {
        // Check if there's a deleted record with the same title and restore it
        if (checkAndRestoreDeletedRecord(currency, "currencies", "title", currency.getTitle())) {
            return currency; // Record was restored
        }
        
        // No deleted record found, proceed with normal save
        // Automatically set position if not set (equals 0)
        setAutoPosition(currency, "currencies");
        
        String sql = "INSERT INTO currencies (create_time, update_time, delete_time, created_by, updated_by, deleted_by, position, title) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Форматируем даты в совместимом с SQLite формате
            String createTimeStr = DateTimeUtil.formatForSqlite(currency.getCreateTime());
            String updateTimeStr = DateTimeUtil.formatForSqlite(currency.getUpdateTime());
            String deleteTimeStr = DateTimeUtil.formatForSqlite(currency.getDeleteTime());
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, currency.getCreatedBy());
            stmt.setString(5, currency.getUpdatedBy());
            stmt.setString(6, currency.getDeletedBy());
            stmt.setInt(7, currency.getPosition());
            stmt.setString(8, currency.getTitle());
            stmt.executeUpdate();
            
            // Получаем сгенерированный id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    currency.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Normalize positions after save
        normalizePositions("currencies");
        
        return currency;
    }

    @Override
    public Optional<Currency> findById(Integer id) {
        return findById("currencies", id, this::mapRowSafe);
    }

    @Override
    public List<Currency> findAll() {
        return findAll("currencies", this::mapRowSafe);
    }

    @Override
    public Currency update(Currency currency) {
        // Adjust positions if needed before updating
        adjustPositionsForUpdate(currency, "currencies", currency.getId());
        
        String sql = "UPDATE currencies SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, position=?, title=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Форматируем даты в совместимом с SQLite формате
            String createTimeStr = DateTimeUtil.formatForSqlite(currency.getCreateTime());
            String updateTimeStr = DateTimeUtil.formatForSqlite(currency.getUpdateTime());
            String deleteTimeStr = DateTimeUtil.formatForSqlite(currency.getDeleteTime());
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, currency.getCreatedBy());
            stmt.setString(5, currency.getUpdatedBy());
            stmt.setString(6, currency.getDeletedBy());
            stmt.setInt(7, currency.getPosition());
            stmt.setString(8, currency.getTitle());
            stmt.setInt(9, currency.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Normalize positions after update
        normalizePositions("currencies");
        
        return currency;
    }

    @Override
    public boolean delete(Integer id) {
        return softDelete("currencies", id, "system");
    }

    /**
     * Soft delete with custom deletedBy parameter
     * @param id entity ID
     * @param deletedBy user who deleted the entity
     * @return true if successful
     */
    public boolean delete(Integer id, String deletedBy) {
        return softDelete("currencies", id, deletedBy);
    }

    /**
     * Restores a soft-deleted currency
     * @param id currency ID
     * @return true if successful
     */
    public boolean restore(Integer id) {
        return restore("currencies", id);
    }

    /**
     * Gets only deleted currencies
     * @return list of deleted currencies
     */
    public List<Currency> findDeleted() {
        return findDeleted("currencies", this::mapRowSafe);
    }

    /**
     * Normalizes positions of all active currencies to be sequential starting from 1
     */
    public void normalizePositions() {
        normalizePositions("currencies");
    }

    private Currency mapRow(ResultSet rs) throws SQLException {
        Currency currency = new Currency();
        currency.setId(rs.getInt("id"));
        
        // Читаем даты как строки и парсим их
        String createTimeStr = rs.getString("create_time");
        currency.setCreateTime(DateTimeUtil.parseFromSqlite(createTimeStr));
        
        String updateTimeStr = rs.getString("update_time");
        currency.setUpdateTime(DateTimeUtil.parseFromSqlite(updateTimeStr));
        
        String deleteTimeStr = rs.getString("delete_time");
        currency.setDeleteTime(DateTimeUtil.parseFromSqlite(deleteTimeStr));
        
        currency.setCreatedBy(rs.getString("created_by"));
        currency.setUpdatedBy(rs.getString("updated_by"));
        currency.setDeletedBy(rs.getString("deleted_by"));
        currency.setPosition(rs.getInt("position"));
        currency.setTitle(rs.getString("title"));
        return currency;
    }

    private Currency mapRowSafe(ResultSet rs) {
        try {
            return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
} 