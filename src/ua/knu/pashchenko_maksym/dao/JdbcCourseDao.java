package ua.knu.pashchenko_maksym.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import ua.knu.pashchenko_maksym.model.Course;

public class JdbcCourseDao implements CourseDao {

    private static final String SELECT_BASE =
            "SELECT id, name, semester, year, teacher_id, credits FROM courses ";

    private static final String SELECT_BY_ID =
            SELECT_BASE + "WHERE id = ?";

    private static final String SELECT_ALL =
            SELECT_BASE + "ORDER BY name";

    private static final String SELECT_BY_NAME =
            SELECT_BASE + "WHERE name = ?";

    private static final String SELECT_BY_TEACHER =
            SELECT_BASE + "WHERE teacher_id = ? ORDER BY name";

    private static final String INSERT_SQL =
            "INSERT INTO courses (name, semester, year, teacher_id, credits) "
                    + "VALUES (?, ?, ?, ?, ?) RETURNING id";

    private static final String UPDATE_SQL =
            "UPDATE courses SET name = ?, semester = ?, year = ?, "
                    + "teacher_id = ?, credits = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM courses WHERE id = ?";

    @Override
    public Course findById(Long id) {
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
            throw new DaoException("Error finding course by id " + id, e);
        }
    }

    @Override
    public List<Course> findAll() {
        List<Course> result = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new DaoException("Error loading all courses", e);
        }
    }

    @Override
    public Course findByName(String name) {
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
            throw new DaoException("Error finding course by name " + name, e);
        }
    }

    @Override
    public List<Course> findByTeacherId(Long teacherId) {
        List<Course> result = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_TEACHER)) {

            ps.setLong(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new DaoException("Error finding courses by teacher " + teacherId, e);
        }
    }

    @Override
    public Course insert(Course course) {
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {

            ps.setString(1, course.getName());

            if (course.getSemester() != null) {
                ps.setShort(2, course.getSemester());
            } else {
                ps.setNull(2, Types.SMALLINT);
            }

            if (course.getYear() != null) {
                ps.setShort(3, course.getYear());
            } else {
                ps.setNull(3, Types.SMALLINT);
            }

            if (course.getTeacherId() != null) {
                ps.setLong(4, course.getTeacherId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            if (course.getCredits() != null) {
                ps.setShort(5, course.getCredits());
            } else {
                ps.setNull(5, Types.SMALLINT);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    course.setId(id);
                }
            }
            return course;

        } catch (SQLException e) {
            throw new DaoException("Error inserting course " + course, e);
        }
    }

    @Override
    public boolean update(Course course) {
        if (course.getId() == null) {
            throw new IllegalArgumentException("Course id must not be null for update");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, course.getName());

            if (course.getSemester() != null) {
                ps.setShort(2, course.getSemester());
            } else {
                ps.setNull(2, Types.SMALLINT);
            }

            if (course.getYear() != null) {
                ps.setShort(3, course.getYear());
            } else {
                ps.setNull(3, Types.SMALLINT);
            }

            if (course.getTeacherId() != null) {
                ps.setLong(4, course.getTeacherId());
            } else {
                ps.setNull(4, Types.BIGINT);
            }

            if (course.getCredits() != null) {
                ps.setShort(5, course.getCredits());
            } else {
                ps.setNull(5, Types.SMALLINT);
            }

            ps.setLong(6, course.getId());

            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            throw new DaoException("Error updating course " + course, e);
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
            throw new DaoException("Error deleting course with id " + id, e);
        }
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getLong("id"));
        course.setName(rs.getString("name"));

        short semester = rs.getShort("semester");
        if (!rs.wasNull()) {
            course.setSemester(semester);
        }

        short year = rs.getShort("year");
        if (!rs.wasNull()) {
            course.setYear(year);
        }

        long teacherId = rs.getLong("teacher_id");
        if (!rs.wasNull()) {
            course.setTeacherId(teacherId);
        }

        short credits = rs.getShort("credits");
        if (!rs.wasNull()) {
            course.setCredits(credits);
        }

        return course;
    }
}

