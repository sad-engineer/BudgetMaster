package repository;

import model.Budget;
import java.sql.*;
import java.util.*;

public class BudgetRepository implements Repository<Budget, Integer> {
    private final String url;

    public BudgetRepository(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public Budget save(Budget budget) {
        String sql = "INSERT INTO budgets (id, create_time, update_time, delete_time, created_by, updated_by, deleted_by, amount, currency_id, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, budget.getId());
            stmt.setObject(2, budget.getCreateTime());
            stmt.setObject(3, budget.getUpdateTime());
            stmt.setObject(4, budget.getDeleteTime());
            stmt.setString(5, budget.getCreatedBy());
            stmt.setString(6, budget.getUpdatedBy());
            stmt.setString(7, budget.getDeletedBy());
            stmt.setInt(8, budget.getAmount());
            stmt.setInt(9, budget.getCurrencyId());
            if (budget.getCategoryId() != null) stmt.setInt(10, budget.getCategoryId()); else stmt.setNull(10, Types.INTEGER);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return budget;
    }

    @Override
    public Optional<Budget> findById(Integer id) {
        String sql = "SELECT * FROM budgets WHERE id = ?";
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
    public List<Budget> findAll() {
        List<Budget> result = new ArrayList<>();
        String sql = "SELECT * FROM budgets";
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
    public Budget update(Budget budget) {
        String sql = "UPDATE budgets SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, amount=?, currency_id=?, category_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, budget.getCreateTime());
            stmt.setObject(2, budget.getUpdateTime());
            stmt.setObject(3, budget.getDeleteTime());
            stmt.setString(4, budget.getCreatedBy());
            stmt.setString(5, budget.getUpdatedBy());
            stmt.setString(6, budget.getDeletedBy());
            stmt.setInt(7, budget.getAmount());
            stmt.setInt(8, budget.getCurrencyId());
            if (budget.getCategoryId() != null) stmt.setInt(9, budget.getCategoryId()); else stmt.setNull(9, Types.INTEGER);
            stmt.setInt(10, budget.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return budget;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM budgets WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Budget mapRow(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setId(rs.getInt("id"));
        budget.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        Timestamp updateTs = rs.getTimestamp("update_time");
        budget.setUpdateTime(updateTs != null ? updateTs.toLocalDateTime() : null);
        Timestamp deleteTs = rs.getTimestamp("delete_time");
        budget.setDeleteTime(deleteTs != null ? deleteTs.toLocalDateTime() : null);
        budget.setCreatedBy(rs.getString("created_by"));
        budget.setUpdatedBy(rs.getString("updated_by"));
        budget.setDeletedBy(rs.getString("deleted_by"));
        budget.setAmount(rs.getInt("amount"));
        budget.setCurrencyId(rs.getInt("currency_id"));
        int val = rs.getInt("category_id");
        budget.setCategoryId(rs.wasNull() ? null : val);
        return budget;
    }
} 