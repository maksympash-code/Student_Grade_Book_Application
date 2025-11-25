package ua.knu.pashchenko_maksym.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import ua.knu.pashchenko_maksym.dao.exception.DaoException;
import ua.knu.pashchenko_maksym.model.Teacher;

public class JdbcTeacherDao implements TeacherDao {

    private static final String SELECT_BASE =
            "SELECT id, first_name, last_name, department, email FROM teachers ";

    private static final String SELECT_BY_ID =
            SELECT_BASE + "WHERE id = ?";

    private static final String SELECT_ALL =
            SELECT_BASE + "ORDER BY last_name, first_name";

    private static final String SELECT_BY_LAST_NAME =
            SELECT_BASE + "WHERE last_name = ? ORDER BY first_name";

    private static final String INSERT_SQL =
            "INSERT INTO teachers(first_name, last_name, department, email) " +
                    "VALUES (?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE teachers SET first_name = ?, last_name = ?, department = ?, email = ? "
                    + "WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM teachers WHERE id = ?";

    @Override
    public Teacher findById(Long id) {
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
            throw new DaoException("Error finding teacher by id " + id, e);
        }
    }

    @Override
    public List<Teacher> findAll() {
        List<Teacher> result = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new DaoException("Error loading all teachers", e);
        }
    }

    @Override
    public List<Teacher> findByLastName(String lastName) {
        List<Teacher> result = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_LAST_NAME)) {

            ps.setString(1, lastName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new DaoException("Error finding teachers by last name " + lastName, e);
        }
    }

    @Override
    public Teacher insert(Teacher teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("teacher must not be null");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, teacher.getFirstName());
            ps.setString(2, teacher.getLastName());
            ps.setString(3, teacher.getDepartment());
            ps.setString(4, teacher.getEmail()); // <-- тепер колонка є

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DaoException("Inserting teacher failed, no rows affected: " + teacher);
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    teacher.setId(id);
                } else {
                    throw new DaoException("Inserting teacher failed, no ID obtained: " + teacher);
                }
            }

            return teacher;
        } catch (SQLException e) {
            throw new DaoException("Error inserting teacher " + teacher, e);
        }
    }


    @Override
    public boolean update(Teacher teacher) {
        if (teacher.getId() == null) {
            throw new IllegalArgumentException("Teacher id must not be null for update");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, teacher.getFirstName());
            ps.setString(2, teacher.getLastName());

            if (teacher.getDepartment() != null) {
                ps.setString(3, teacher.getDepartment());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }

            if (teacher.getEmail() != null) {
                ps.setString(4, teacher.getEmail());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            ps.setLong(5, teacher.getId());

            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            throw new DaoException("Error updating teacher " + teacher, e);
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
            throw new DaoException("Error deleting teacher with id " + id, e);
        }
    }

    private Teacher mapRow(ResultSet rs) throws SQLException {
        Teacher teacher = new Teacher();
        teacher.setId(rs.getLong("id"));
        teacher.setFirstName(rs.getString("first_name"));
        teacher.setLastName(rs.getString("last_name"));
        teacher.setDepartment(rs.getString("department"));
        teacher.setEmail(rs.getString("email"));
        return teacher;
    }
}

