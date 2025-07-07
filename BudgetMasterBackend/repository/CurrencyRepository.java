// -*- coding: utf-8 -*-
package repository;

import model.Currency;
import util.DateTimeUtil;
import java.sql.*;
import java.util.*;

public class CurrencyRepository implements Repository<Currency, Integer> {
    private final String url;

    public CurrencyRepository(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
    }

    private Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        // Устанавливаем кодировку UTF-8 для подключения
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA encoding = 'UTF-8'");
        }
        return conn;
    }

    @Override
    public Currency save(Currency currency) {
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
        return currency;
    }

    @Override
    public Optional<Currency> findById(Integer id) {
        String sql = "SELECT * FROM currencies WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Currency> findAll() {
        List<Currency> result = new ArrayList<>();
        String sql = "SELECT * FROM currencies";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Currency update(Currency currency) {
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
        return currency;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM currencies WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
} 