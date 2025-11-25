package ua.knu.pashchenko_maksym.dao;

import ua.knu.pashchenko_maksym.dao.exception.DaoException;
import ua.knu.pashchenko_maksym.model.Group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of GroupDao for table "groups".
 */
public class JdbcGroupDao implements GroupDao {

    private static final String FIND_BY_ID_SQL =
            "SELECT id, name, year FROM groups WHERE id = ?";

    private static final String FIND_ALL_SQL =
            "SELECT id, name, year FROM groups ORDER BY name";

    private static final String INSERT_SQL =
            "INSERT INTO groups(name, year) VALUES (?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE groups SET name = ?, year = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM groups WHERE id = ?";

    private static final String FIND_BY_NAME_SQL =
            "SELECT id, name, year FROM groups WHERE name = ?";

    // =============================
    // findById (Long) + findById(long)
    // =============================

    @Override
    public Group findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return findByIdInternal(id);
    }

    @Override
    public Optional<Group> findById(long id) {
        return Optional.ofNullable(findByIdInternal(id));
    }

    private Group findByIdInternal(long id) {
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_ID_SQL)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error finding group by id = " + id, e);
        }
    }

    // =============================
    // findAll
    // =============================

    @Override
    public List<Group> findAll() {
        List<Group> result = new ArrayList<>();

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }

            return result;
        } catch (SQLException e) {
            throw new DaoException("Error loading all groups", e);
        }
    }

    // =============================
    // findByName
    // =============================

    @Override
    public Group findByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_NAME_SQL)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

            return null;
        } catch (SQLException e) {
            throw new DaoException("Error finding group by name = " + name, e);
        }
    }

    // =============================
    // insert
    // =============================

    @Override
    public Group insert(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, group.getName());
            if (group.getYear() != null) {
                ps.setShort(2, group.getYear());
            } else {
                ps.setNull(2, java.sql.Types.SMALLINT);
            }

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DaoException("Inserting group failed, no rows affected: " + group);
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    group.setId(id);
                } else {
                    throw new DaoException("Inserting group failed, no ID obtained: " + group);
                }
            }

            return group;
        } catch (SQLException e) {
            throw new DaoException("Error inserting group " + group, e);
        }
    }

    // =============================
    // update
    // =============================

    @Override
    public boolean update(Group group) {
        if (group == null || group.getId() == null) {
            throw new IllegalArgumentException("group and group.id must not be null");
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

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating group " + group, e);
        }
    }

    // =============================
    // delete(Long) + delete(long)
    // =============================

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return delete(id.longValue());
    }

    @Override
    public boolean delete(long id) {
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE_SQL)) {

            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting group with id = " + id, e);
        }
    }

    // =============================
    // mapper
    // =============================

    private Group mapRow(ResultSet rs) throws SQLException {
        Group group = new Group();
        group.setId(rs.getLong("id"));
        group.setName(rs.getString("name"));
        group.setYear(rs.getShort("year"));
        return group;
    }
}
