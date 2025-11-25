package ua.knu.pashchenko_maksym.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ua.knu.pashchenko_maksym.dao.exception.DaoException;
import ua.knu.pashchenko_maksym.model.Student;

public class JdbcStudentDao implements StudentDao {

    private static final String SELECT_BASE =
            "SELECT id, first_name, last_name, email, group_id, "
                    + "enrollment_year, created_at FROM students ";

    private static final String SELECT_BY_ID =
            SELECT_BASE + "WHERE id = ?";

    private static final String SELECT_ALL =
            SELECT_BASE + "ORDER BY last_name, first_name";

    private static final String SELECT_BY_GROUP =
            SELECT_BASE + "WHERE group_id = ? ORDER BY last_name, first_name";

    private static final String SELECT_BY_COURSE =
            "SELECT DISTINCT s.id, s.first_name, s.last_name, s.email, "
                    + "s.group_id, s.enrollment_year, s.created_at "
                    + "FROM students s "
                    + "JOIN grades g ON g.student_id = s.id "
                    + "WHERE g.course_id = ? "
                    + "ORDER BY s.last_name, s.first_name";

    private static final String INSERT_SQL =
            "INSERT INTO students (first_name, last_name, email, group_id, enrollment_year) "
                    + "VALUES (?, ?, ?, ?, ?) RETURNING id, created_at";

    private static final String UPDATE_SQL =
            "UPDATE students SET first_name = ?, last_name = ?, email = ?, "
                    + "group_id = ?, enrollment_year = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM students WHERE id = ?";

    @Override
    public Student findById(Long id) {
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
            throw new DaoException("Error finding student by id " + id, e);
        }
    }

    @Override
    public List<Student> findAll() {
        List<Student> result = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new DaoException("Error loading all students", e);
        }
    }

    @Override
    public Student insert(Student student) {
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {

            ps.setString(1, student.getFirstName());
            ps.setString(2, student.getLastName());
            ps.setString(3, student.getEmail());

            if (student.getGroupId() != null) {
                ps.setLong(4, student.getGroupId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            if (student.getEnrollmentYear() != null) {
                ps.setShort(5, student.getEnrollmentYear());
            } else {
                ps.setNull(5, Types.SMALLINT);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    Timestamp createdTs = rs.getTimestamp("created_at");
                    student.setId(id);
                    if (createdTs != null) {
                        student.setCreatedAt(createdTs.toLocalDateTime());
                    }
                }
            }

            return student;

        } catch (SQLException e) {
            throw new DaoException("Error inserting student " + student, e);
        }
    }

    @Override
    public boolean update(Student student) {
        if (student.getId() == null) {
            throw new IllegalArgumentException("Student id must not be null for update");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, student.getFirstName());
            ps.setString(2, student.getLastName());
            ps.setString(3, student.getEmail());

            if (student.getGroupId() != null) {
                ps.setLong(4, student.getGroupId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            if (student.getEnrollmentYear() != null) {
                ps.setShort(5, student.getEnrollmentYear());
            } else {
                ps.setNull(5, Types.SMALLINT);
            }

            ps.setLong(6, student.getId());

            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            throw new DaoException("Error updating student " + student, e);
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
            throw new DaoException("Error deleting student with id " + id, e);
        }
    }

    @Override
    public List<Student> findByGroupId(Long groupId) {
        List<Student> result = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_GROUP)) {

            ps.setLong(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new DaoException("Error finding students by group " + groupId, e);
        }
    }

    @Override
    public List<Student> findByCourseId(Long courseId) {
        List<Student> result = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_COURSE)) {

            ps.setLong(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new DaoException("Error finding students by course " + courseId, e);
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getLong("id"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setEmail(rs.getString("email"));

        long groupId = rs.getLong("group_id");
        if (!rs.wasNull()) {
            student.setGroupId(groupId);
        }

        short year = rs.getShort("enrollment_year");
        if (!rs.wasNull()) {
            student.setEnrollmentYear(year);
        }

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            LocalDateTime created = ts.toLocalDateTime();
            student.setCreatedAt(created);
        }

        return student;
    }
}

