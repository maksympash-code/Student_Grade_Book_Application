package main.java.ua.knu.pashchenko_maksym.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import main.java.ua.knu.pashchenko_maksym.model.Group;

/**
 * JDBC implementation of GroupDao.
 */
public class JdbcGroupDao implements GroupDao {

    private static final String SELECT_BY_ID =
            "SELECT id, name, year FROM groups WHERE id = ?";

    private static final String SELECT_ALL =
            "SELECT id, name, year FROM groups ORDER BY name";

    private static final String SELECT_BY_NAME =
            "SELECT id, name, year FROM groups WHERE name = ?";

    private static final String INSERT_SQL =
            "INSERT INTO groups (name, year) VALUES (?, ?) RETURNING id";

    private static final String UPDATE_SQL =
            "UPDATE groups SET name = ?, year = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM groups WHERE id = ?";

    @Override
    public Group findById(Long id) {
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DaoException("Error finding group by id " + id, e);
        }
    }

    @Override
    public List<Group> findAll() {
        List<Group> result = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new DaoException("Error loading all groups", e);
        }
    }

    @Override
    public Group findByName(String name) {
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_NAME)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }

        } catch (SQLException e) {
            throw new DaoException("Error finding group by name " + name, e);
        }
    }

    @Override
    public Group insert(Group group) {
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {

            ps.setString(1, group.getName());
            if (group.getYear() != null) {
                ps.setShort(2, group.getYear());
            } else {
                ps.setNull(2, java.sql.Types.SMALLINT);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    group.setId(id);
                }
            }
            return group;

        } catch (SQLException e) {
            throw new DaoException("Error inserting group " + group, e);
        }
    }

    @Override
    public boolean update(Group group) {
        if (group.getId() == null) {
            throw new IllegalArgumentException("Group id must not be null for update");
        }
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, group.getName());
            if (group.getYear() != null) {
                ps.setShort(2, group.getYear());
            } else {
                ps.setNull(2, java.sql.Types.SMALLINT);
            }
            ps.setLong(3, group.getId());

            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            throw new DaoException("Error updating group " + group, e);
        }
    }

    @Override
    public boolean delete(Long id) {
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE_SQL)) {

            ps.setLong(1, id);
            int deleted = ps.executeUpdate();
            return deleted > 0;

        } catch (SQLException e) {
            throw new DaoException("Error deleting group with id " + id, e);
        }
    }

    private Group mapRow(ResultSet rs) throws SQLException {
        Group group = new Group();
        group.setId(rs.getLong("id"));
        group.setName(rs.getString("name"));
        short year = rs.getShort("year");
        if (!rs.wasNull()) {
            group.setYear(year);
        }
        return group;
    }
}

