package repository;

import model.Operation;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class OperationRepository implements Repository<Operation, Integer> {
    private final String url;

    public OperationRepository(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public Operation save(Operation op) {
        String sql = "INSERT INTO operations (id, create_time, update_time, delete_time, created_by, updated_by, deleted_by, type, date, amount, comment, category_id, account_id, currency_id, to_account_id, to_currency_id, to_amount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, op.getId());
            stmt.setObject(2, op.getCreateTime());
            stmt.setObject(3, op.getUpdateTime());
            stmt.setObject(4, op.getDeleteTime());
            stmt.setString(5, op.getCreatedBy());
            stmt.setString(6, op.getUpdatedBy());
            stmt.setString(7, op.getDeletedBy());
            stmt.setInt(8, op.getType());
            stmt.setObject(9, op.getDate());
            stmt.setInt(10, op.getAmount());
            stmt.setString(11, op.getComment());
            stmt.setInt(12, op.getCategoryId());
            stmt.setInt(13, op.getAccountId());
            stmt.setInt(14, op.getCurrencyId());
            if (op.getToAccountId() != null) stmt.setInt(15, op.getToAccountId()); else stmt.setNull(15, Types.INTEGER);
            if (op.getToCurrencyId() != null) stmt.setInt(16, op.getToCurrencyId()); else stmt.setNull(16, Types.INTEGER);
            if (op.getToAmount() != null) stmt.setInt(17, op.getToAmount()); else stmt.setNull(17, Types.INTEGER);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return op;
    }

    @Override
    public Optional<Operation> findById(Integer id) {
        String sql = "SELECT * FROM operations WHERE id = ?";
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
        String sql = "SELECT * FROM operations";
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
            stmt.setObject(1, op.getCreateTime());
            stmt.setObject(2, op.getUpdateTime());
            stmt.setObject(3, op.getDeleteTime());
            stmt.setString(4, op.getCreatedBy());
            stmt.setString(5, op.getUpdatedBy());
            stmt.setString(6, op.getDeletedBy());
            stmt.setInt(7, op.getType());
            stmt.setObject(8, op.getDate());
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
        String sql = "DELETE FROM operations WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
        
        // Безопасное чтение дат
        String createTimeStr = rs.getString("create_time");
        op.setCreateTime(createTimeStr != null ? LocalDateTime.parse(createTimeStr.replace(' ', 'T')) : null);
        
        String updateTimeStr = rs.getString("update_time");
        op.setUpdateTime(updateTimeStr != null ? LocalDateTime.parse(updateTimeStr.replace(' ', 'T')) : null);
        
        String deleteTimeStr = rs.getString("delete_time");
        op.setDeleteTime(deleteTimeStr != null ? LocalDateTime.parse(deleteTimeStr.replace(' ', 'T')) : null);
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
        op.setDate(dateStr != null ? LocalDateTime.parse(dateStr.replace(' ', 'T')) : null);
        
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
} 