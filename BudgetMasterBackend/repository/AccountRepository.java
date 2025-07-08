package repository;

import model.Account;
import util.DateTimeUtil;
import java.sql.*;
import java.util.*;

public class AccountRepository extends BaseRepository implements Repository<Account, Integer> {

    public AccountRepository(String dbPath) {
        super(dbPath);
    }

    @Override
    public Account save(Account account) {
        // Check if there's a deleted record with the same title and restore it
        if (checkAndRestoreDeletedRecord(account, "accounts", "title", account.getTitle())) {
            return account; // Record was restored
        }
        
        // Automatically set position if not set (equals 0)
        setAutoPosition(account, "accounts");
        
        String sql = "INSERT INTO accounts (create_time, update_time, delete_time, created_by, updated_by, deleted_by, position, title, amount, type, currency_id, closed, credit_card_limit, credit_card_category_id, credit_card_commission_category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Форматируем даты в совместимом с SQLite формате
            String createTimeStr = DateTimeUtil.formatForSqlite(account.getCreateTime());
            String updateTimeStr = DateTimeUtil.formatForSqlite(account.getUpdateTime());
            String deleteTimeStr = DateTimeUtil.formatForSqlite(account.getDeleteTime());
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, account.getCreatedBy());
            stmt.setString(5, account.getUpdatedBy());
            stmt.setString(6, account.getDeletedBy());
            stmt.setInt(7, account.getPosition());
            stmt.setString(8, account.getTitle());
            stmt.setInt(9, account.getAmount());
            stmt.setInt(10, account.getType());
            stmt.setInt(11, account.getCurrencyId());
            stmt.setInt(12, account.getClosed());
            if (account.getCreditCardLimit() != null) stmt.setInt(13, account.getCreditCardLimit()); else stmt.setNull(13, Types.INTEGER);
            if (account.getCreditCardCategoryId() != null) stmt.setInt(14, account.getCreditCardCategoryId()); else stmt.setNull(14, Types.INTEGER);
            if (account.getCreditCardCommissionCategoryId() != null) stmt.setInt(15, account.getCreditCardCommissionCategoryId()); else stmt.setNull(15, Types.INTEGER);
            stmt.executeUpdate();
            
            // Получаем сгенерированный id
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    account.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Normalize positions after save
        normalizePositions("accounts");
        
        return account;
    }

    @Override
    public Optional<Account> findById(Integer id) {
        return findById("accounts", id, this::mapRowSafe);
    }

    @Override
    public List<Account> findAll() {
        return findAll("accounts", this::mapRowSafe);
    }

    @Override
    public Account update(Account account) {
        // Adjust positions if needed before updating
        adjustPositionsForUpdate(account, "accounts", account.getId());
        
        String sql = "UPDATE accounts SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, position=?, title=?, amount=?, type=?, currency_id=?, closed=?, credit_card_limit=?, credit_card_category_id=?, credit_card_commission_category_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Форматируем даты в совместимом с SQLite формате
            String createTimeStr = DateTimeUtil.formatForSqlite(account.getCreateTime());
            String updateTimeStr = DateTimeUtil.formatForSqlite(account.getUpdateTime());
            String deleteTimeStr = DateTimeUtil.formatForSqlite(account.getDeleteTime());
            
            stmt.setString(1, createTimeStr);
            stmt.setString(2, updateTimeStr);
            stmt.setString(3, deleteTimeStr);
            stmt.setString(4, account.getCreatedBy());
            stmt.setString(5, account.getUpdatedBy());
            stmt.setString(6, account.getDeletedBy());
            stmt.setInt(7, account.getPosition());
            stmt.setString(8, account.getTitle());
            stmt.setInt(9, account.getAmount());
            stmt.setInt(10, account.getType());
            stmt.setInt(11, account.getCurrencyId());
            stmt.setInt(12, account.getClosed());
            if (account.getCreditCardLimit() != null) stmt.setInt(13, account.getCreditCardLimit()); else stmt.setNull(13, Types.INTEGER);
            if (account.getCreditCardCategoryId() != null) stmt.setInt(14, account.getCreditCardCategoryId()); else stmt.setNull(14, Types.INTEGER);
            if (account.getCreditCardCommissionCategoryId() != null) stmt.setInt(15, account.getCreditCardCommissionCategoryId()); else stmt.setNull(15, Types.INTEGER);
            stmt.setInt(16, account.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Normalize positions after update
        normalizePositions("accounts");
        
        return account;
    }

    @Override
    public boolean delete(Integer id) {
        return softDelete("accounts", id, "system");
    }

    /**
     * Soft delete with custom deletedBy parameter
     * @param id entity ID
     * @param deletedBy user who deleted the entity
     * @return true if successful
     */
    public boolean delete(Integer id, String deletedBy) {
        return softDelete("accounts", id, deletedBy);
    }

    /**
     * Restores a soft-deleted account
     * @param id account ID
     * @return true if successful
     */
    public boolean restore(Integer id) {
        return restore("accounts", id);
    }

    /**
     * Gets only deleted accounts
     * @return list of deleted accounts
     */
    public List<Account> findDeleted() {
        return findDeleted("accounts", this::mapRowSafe);
    }

    /**
     * Normalizes positions of all active accounts to be sequential starting from 1
     */
    public void normalizePositions() {
        normalizePositions("accounts");
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getInt("id"));
        
        // Читаем даты как строки и парсим их
        String createTimeStr = rs.getString("create_time");
        account.setCreateTime(DateTimeUtil.parseFromSqlite(createTimeStr));
        
        String updateTimeStr = rs.getString("update_time");
        account.setUpdateTime(DateTimeUtil.parseFromSqlite(updateTimeStr));
        
        String deleteTimeStr = rs.getString("delete_time");
        account.setDeleteTime(DateTimeUtil.parseFromSqlite(deleteTimeStr));
        
        account.setCreatedBy(rs.getString("created_by"));
        account.setUpdatedBy(rs.getString("updated_by"));
        account.setDeletedBy(rs.getString("deleted_by"));
        account.setPosition(rs.getInt("position"));
        account.setTitle(rs.getString("title"));
        account.setAmount(rs.getInt("amount"));
        account.setType(rs.getInt("type"));
        account.setCurrencyId(rs.getInt("currency_id"));
        account.setClosed(rs.getInt("closed"));
        int val;
        val = rs.getInt("credit_card_limit");
        account.setCreditCardLimit(rs.wasNull() ? null : val);
        val = rs.getInt("credit_card_category_id");
        account.setCreditCardCategoryId(rs.wasNull() ? null : val);
        val = rs.getInt("credit_card_commission_category_id");
        account.setCreditCardCommissionCategoryId(rs.wasNull() ? null : val);
        return account;
    }

    private Account mapRowSafe(ResultSet rs) {
        try {
            return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
} 