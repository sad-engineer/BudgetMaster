package repository;

import model.Category;
import java.sql.*;
import java.util.*;

public class CategoryRepository implements Repository<Category, Integer> {
    private final String url;

    public CategoryRepository(String dbPath) {
        this.url = "jdbc:sqlite:" + dbPath;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url);
    }

    @Override
    public Category save(Category category) {
        String sql = "INSERT INTO categories (id, create_time, update_time, delete_time, created_by, updated_by, deleted_by, position, title, operation_type, type, parent_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, category.getId());
            stmt.setObject(2, category.getCreateTime());
            stmt.setObject(3, category.getUpdateTime());
            stmt.setObject(4, category.getDeleteTime());
            stmt.setString(5, category.getCreatedBy());
            stmt.setString(6, category.getUpdatedBy());
            stmt.setString(7, category.getDeletedBy());
            stmt.setInt(8, category.getPosition());
            stmt.setString(9, category.getTitle());
            stmt.setInt(10, category.getOperationType());
            stmt.setInt(11, category.getType());
            if (category.getParentId() != null) stmt.setInt(12, category.getParentId()); else stmt.setNull(12, Types.INTEGER);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return category;
    }

    @Override
    public Optional<Category> findById(Integer id) {
        String sql = "SELECT * FROM categories WHERE id = ?";
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
    public List<Category> findAll() {
        List<Category> result = new ArrayList<>();
        String sql = "SELECT * FROM categories";
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
    public Category update(Category category) {
        String sql = "UPDATE categories SET create_time=?, update_time=?, delete_time=?, created_by=?, updated_by=?, deleted_by=?, position=?, title=?, operation_type=?, type=?, parent_id=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, category.getCreateTime());
            stmt.setObject(2, category.getUpdateTime());
            stmt.setObject(3, category.getDeleteTime());
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
        return category;
    }

    @Override
    public boolean delete(Integer id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Category mapRow(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getInt("id"));
        category.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
        Timestamp updateTs = rs.getTimestamp("update_time");
        category.setUpdateTime(updateTs != null ? updateTs.toLocalDateTime() : null);
        Timestamp deleteTs = rs.getTimestamp("delete_time");
        category.setDeleteTime(deleteTs != null ? deleteTs.toLocalDateTime() : null);
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
} 