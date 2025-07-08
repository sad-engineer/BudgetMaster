package repository;

import model.Operation;
import util.DateTimeUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class OperationRepository extends BaseRepository implements Repository<Operation, Integer> {

    public OperationRepository(String dbPath) {
        super(dbPath);
    }

    @Override
    public Operation save(Operation op) {
        String sql = "INSERT INTO operations (create_time, update_time, delete_time, created_by, updated_by, deleted_by, type, date, amount, comment, category_id, account_id, currency_id, to_account_id, to_currency_id, to_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Форматируем даты в совместимом с SQLite формате
            String createTimeStr = DateTimeUtil.formatForSqlite(op.getCreateTime());
            String updateTimeStr = DateTimeUtil.formatForSqlite(op.getUpdateTime());
            String deleteTimeStr = DateTimeUtil.formatForSqlite(op.getDeleteTime());
            String dateStr = DateTimeUtil.formatForSqlite(op.getDate());
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, op.getCreatedBy());
            stmt.setString(5, op.getUpdatedBy());
            stmt.setString(6, op.getDeletedBy());
            stmt.setInt(7, op.getType());
            stmt.setString(8, dateStr);
            stmt.setInt(9, op.getAmount());
            stmt.setString(10, op.getComment());
            stmt.setInt(11, op.getCategoryId());
            stmt.setInt(12, op.getAccountId());
            stmt.setInt(13, op.getCurrencyId());
            if (op.getToAccountId() != null) stmt.setInt(14, op.getToAccountId()); else stmt.setNull(14, Types.INTEGER);
            if (op.getToCurrencyId() != null) stmt.setInt(15, op.getToCurrencyId()); else stmt.setNull(15, Types.INTEGER);
            if (op.getToAmount() != null) stmt.setInt(16, op.getToAmount()); else stmt.setNull(16, Types.INTEGER);
            stmt.executeUpdate();
            
            // Получаем сгенерированный id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    op.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return op;
    }

    @Override
    public Optional<Operation> findById(Integer id) {
        String sql = "SELECT * FROM operations WHERE id = ?" + getDeletedFilterAndClause();
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
    public List<Operation> findAll() {
        List<Operation> result = new ArrayList<>();
        String sql = "SELECT * FROM operations" + getDeletedFilterClause();
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
    public Operation update(Operation op) {
        String sql = "UPDATE operations SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, type=?, date=?, amount=?, comment=?, category_id=?, account_id=?, currency_id=?, to_account_id=?, to_currency_id=?, to_amount=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Форматируем даты в совместимом с SQLite формате
            String createTimeStr = DateTimeUtil.formatForSqlite(op.getCreateTime());
            String updateTimeStr = DateTimeUtil.formatForSqlite(op.getUpdateTime());
            String deleteTimeStr = DateTimeUtil.formatForSqlite(op.getDeleteTime());
            String dateStr = DateTimeUtil.formatForSqlite(op.getDate());
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, op.getCreatedBy());
            stmt.setString(5, op.getUpdatedBy());
            stmt.setString(6, op.getDeletedBy());
            stmt.setInt(7, op.getType());
            stmt.setString(8, dateStr);
            stmt.setInt(9, op.getAmount());
            stmt.setString(10, op.getComment());
            stmt.setInt(11, op.getCategoryId());
            stmt.setInt(12, op.getAccountId());
            stmt.setInt(13, op.getCurrencyId());
            if (op.getToAccountId() != null) stmt.setInt(14, op.getToAccountId()); else stmt.setNull(14, Types.INTEGER);
            if (op.getToCurrencyId() != null) stmt.setInt(15, op.getToCurrencyId()); else stmt.setNull(15, Types.INTEGER);
            if (op.getToAmount() != null) stmt.setInt(16, op.getToAmount()); else stmt.setNull(16, Types.INTEGER);
            stmt.setInt(17, op.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return op;
    }

    @Override
    public boolean delete(Integer id) {
        return delete(id, "system");
    }

    /**
     * Soft delete с параметром deletedBy
     * @param id идентификатор сущности
     * @param deletedBy пользователь, который удалил сущность
     * @return true, если удаление успешно
     */
    public boolean delete(Integer id, String deletedBy) {
        String sql = "UPDATE operations SET delete_time = ?, deleted_by = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String deleteTimeStr = DateTimeUtil.formatForSqlite(java.time.LocalDateTime.now());
            stmt.setString(1, deleteTimeStr);
            stmt.setString(2, deletedBy);
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Восстанавливает удаленную операцию
     * @param id идентификатор операции
     * @return true, если восстановление успешно
     */
    public boolean restore(Integer id) {
        String sql = "UPDATE operations SET delete_time = NULL, deleted_by = NULL WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Получает только удаленные операции
     * @return список удаленных операций
     */
    public List<Operation> findDeleted() {
        List<Operation> result = new ArrayList<>();
        String sql = "SELECT * FROM operations WHERE delete_time IS NOT NULL";
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

    /**
     * Получает операции, отсортированные по дате (сначала новые)
     * @return список операций, отсортированных по дате
     */
    public List<Operation> findAllOrderedByDate() {
        List<Operation> result = new ArrayList<>();
        String sql = "SELECT * FROM operations" + getDeletedFilterClause() + " ORDER BY date DESC";
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

    private Operation mapRow(ResultSet rs) throws SQLException {
        Operation op = new Operation();
        
        // Безопасное чтение ID
        Object idObj = rs.getObject("id");
        if (idObj instanceof Long) {
            op.setId(((Long) idObj).intValue());
        } else {
            op.setId((Integer) idObj);
        }
        
        // Читаем даты как строки и парсим их
        String createTimeStr = rs.getString("create_time");
        op.setCreateTime(DateTimeUtil.parseFromSqlite(createTimeStr));
        
        String updateTimeStr = rs.getString("update_time");
        op.setUpdateTime(DateTimeUtil.parseFromSqlite(updateTimeStr));
        
        String deleteTimeStr = rs.getString("delete_time");
        op.setDeleteTime(DateTimeUtil.parseFromSqlite(deleteTimeStr));
        
        op.setCreatedBy(rs.getString("created_by"));
        op.setUpdatedBy(rs.getString("updated_by"));
        op.setDeletedBy(rs.getString("deleted_by"));
        
        // Безопасное чтение числовых полей
        Object typeObj = rs.getObject("type");
        if (typeObj instanceof Long) {
            op.setType(((Long) typeObj).intValue());
        } else {
            op.setType((Integer) typeObj);
        }
        
        // Безопасное чтение даты операции
        String dateStr = rs.getString("date");
        op.setDate(DateTimeUtil.parseFromSqlite(dateStr));
        
        Object amountObj = rs.getObject("amount");
        if (amountObj instanceof Long) {
            op.setAmount(((Long) amountObj).intValue());
        } else {
            op.setAmount((Integer) amountObj);
        }
        
        op.setComment(rs.getString("comment"));
        
        Object categoryIdObj = rs.getObject("category_id");
        if (categoryIdObj instanceof Long) {
            op.setCategoryId(((Long) categoryIdObj).intValue());
        } else {
            op.setCategoryId((Integer) categoryIdObj);
        }
        
        Object accountIdObj = rs.getObject("account_id");
        if (accountIdObj instanceof Long) {
            op.setAccountId(((Long) accountIdObj).intValue());
        } else {
            op.setAccountId((Integer) accountIdObj);
        }
        
        Object currencyIdObj = rs.getObject("currency_id");
        if (currencyIdObj instanceof Long) {
            op.setCurrencyId(((Long) currencyIdObj).intValue());
        } else {
            op.setCurrencyId((Integer) currencyIdObj);
        }
        
        // Обработка nullable полей
        Object toAccountIdObj = rs.getObject("to_account_id");
        if (toAccountIdObj == null) {
            op.setToAccountId(null);
        } else if (toAccountIdObj instanceof Long) {
            op.setToAccountId(((Long) toAccountIdObj).intValue());
        } else {
            op.setToAccountId((Integer) toAccountIdObj);
        }
        
        Object toCurrencyIdObj = rs.getObject("to_currency_id");
        if (toCurrencyIdObj == null) {
            op.setToCurrencyId(null);
        } else if (toCurrencyIdObj instanceof Long) {
            op.setToCurrencyId(((Long) toCurrencyIdObj).intValue());
        } else {
            op.setToCurrencyId((Integer) toCurrencyIdObj);
        }
        
        Object toAmountObj = rs.getObject("to_amount");
        if (toAmountObj == null) {
            op.setToAmount(null);
        } else if (toAmountObj instanceof Long) {
            op.setToAmount(((Long) toAmountObj).intValue());
        } else {
            op.setToAmount((Integer) toAmountObj);
        }
        
        return op;
    }

    private Operation mapRowSafe(ResultSet rs) {
        try {
            return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
} 