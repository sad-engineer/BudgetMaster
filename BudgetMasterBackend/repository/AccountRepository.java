package repository;

import model.Account;
import java.sql.*;
import java.util.*;

public class AccountRepository implements Repository<Account, Integer> {
    private final String url;

    public AccountRepository(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public Account save(Account account) {
        String sql = "INSERT INTO accounts (id, create_time, update_time, delete_time, created_by, updated_by, deleted_by, position, title, amount, type, currency_id, closed, credit_card_limit, credit_card_category_id, credit_card_commission_category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, account.getId());
            stmt.setObject(2, account.getCreateTime());
            stmt.setObject(3, account.getUpdateTime());
            stmt.setObject(4, account.getDeleteTime());
            stmt.setString(5, account.getCreatedBy());
            stmt.setString(6, account.getUpdatedBy());
            stmt.setString(7, account.getDeletedBy());
            stmt.setInt(8, account.getPosition());
            stmt.setString(9, account.getTitle());
            stmt.setInt(10, account.getAmount());
            stmt.setInt(11, account.getType());
            stmt.setInt(12, account.getCurrencyId());
            stmt.setInt(13, account.getClosed());
            if (account.getCreditCardLimit() != null) stmt.setInt(14, account.getCreditCardLimit()); else stmt.setNull(14, Types.INTEGER);
            if (account.getCreditCardCategoryId() != null) stmt.setInt(15, account.getCreditCardCategoryId()); else stmt.setNull(15, Types.INTEGER);
            if (account.getCreditCardCommissionCategoryId() != null) stmt.setInt(16, account.getCreditCardCommissionCategoryId()); else stmt.setNull(16, Types.INTEGER);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return account;
    }

    @Override
    public Optional<Account> findById(Integer id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
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
    public List<Account> findAll() {
        List<Account> result = new ArrayList<>();
        String sql = "SELECT * FROM accounts";
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
    public Account update(Account account) {
        String sql = "UPDATE accounts SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, position=?, title=?, amount=?, type=?, currency_id=?, closed=?, credit_card_limit=?, credit_card_category_id=?, credit_card_commission_category_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, account.getCreateTime());
            stmt.setObject(2, account.getUpdateTime());
            stmt.setObject(3, account.getDeleteTime());
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
        return account;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setId(rs.getInt("id"));
        account.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        Timestamp updateTs = rs.getTimestamp("update_time");
        account.setUpdateTime(updateTs != null ? updateTs.toLocalDateTime() : null);
        Timestamp deleteTs = rs.getTimestamp("delete_time");
        account.setDeleteTime(deleteTs != null ? deleteTs.toLocalDateTime() : null);
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
} 