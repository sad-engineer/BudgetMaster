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
        return findById("currencies", id, this::mapRowSafe);
    }

    @Override
    public List<Currency> findAll() {
        return findAll("currencies", this::mapRowSafe);
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
        return softDelete("currencies", id, "system");
    }

    /**
     * Soft delete с пользовательским параметром deletedBy
     * @param id ID сущности
     * @param deletedBy пользователь, который удалил сущность
     * @return true, если удаление успешно
     */
    public boolean delete(Integer id, String deletedBy) {
        return softDelete("currencies", id, deletedBy);
    }

    /**
     * Восстанавливает удаленную валюту
     * @param id ID валюты
     * @return true, если восстановление успешно
     */
    public boolean restore(Integer id) {
        return restore("currencies", id);
    }

    /**
     * Получает только удаленные записи
     * @return список удаленных записей
     */
    public List<Currency> findDeleted() {
        return findDeleted("currencies", this::mapRowSafe);
    }

    /**
     * Проверяет, есть ли удаленная запись с таким же title
     * @param title заголовок валюты
     * @return Optional с ID удаленной записи, если найдена
     */
    public Optional<Integer> findDeletedByTitle(String title) {
        String sql = "SELECT id FROM currencies WHERE title = ? AND delete_time IS NOT NULL";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Получает следующую доступную позицию
     * @return следующая позиция
     */
    public int getNextPosition() {
        return getNextPosition("currencies");
    }

    /**
     * Нормализует позиции всех активных валют
     */
    public void normalizePositions() {
        normalizePositions("currencies");
    }

    /**
     * Корректирует позиции при обновлении
     * @param currency валюта для обновления
     */
    public void adjustPositionsForUpdate(Currency currency) {
        adjustPositionsForUpdate(currency, "currencies", currency.getId());
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