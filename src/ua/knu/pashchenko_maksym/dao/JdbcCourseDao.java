package ua.knu.pashchenko_maksym.dao;

import ua.knu.pashchenko_maksym.dao.exception.DaoException;
import ua.knu.pashchenko_maksym.model.Course;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of {@link CourseDao} for the {@code courses} table.
 *
 * <p>Виконує базові CRUD-операції та запити за викладачем
 * через простий {@link java.sql.Connection} / {@link PreparedStatement}.
 *
 * @author Pashchenko Maksym
 * @since 26.11.2025
 */
public class JdbcCourseDao implements CourseDao {

    private static final String SELECT_BASE =
            "SELECT id, name, semester, year, teacher_id FROM courses";

    private static final String FIND_BY_ID_SQL =
            SELECT_BASE + " WHERE id = ?";

    private static final String FIND_ALL_SQL =
            SELECT_BASE + " ORDER BY id";

    private static final String FIND_BY_NAME_SQL =
            SELECT_BASE + " WHERE name = ?";

    private static final String FIND_BY_TEACHER_SQL =
            SELECT_BASE + " WHERE teacher_id = ? ORDER BY id";

    private static final String INSERT_SQL =
            "INSERT INTO courses(name, semester, year, teacher_id) " +
                    "VALUES (?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE courses SET name = ?, semester = ?, year = ?, teacher_id = ? " +
                    "WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM courses WHERE id = ?";

    /**
     * Finds course by its primary key.
     *
     * @param id course identifier
     * @return {@link Course} instance or {@code null}, if not found or id is {@code null}
     * @throws DaoException if a database access error occurs
     */
    @Override
    public Course findById(Long id) {
        if (id == null) {
            return null;
        }

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
            throw new DaoException("Error finding course by id=" + id, e);
        }
    }

    /**
     * Returns all courses ordered by id.
     *
     * @return list of courses (possibly empty, never {@code null})
     * @throws DaoException if a database access error occurs
     */
    @Override
    public List<Course> findAll() {
        List<Course> result = new ArrayList<>();

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }

            return result;
        } catch (SQLException e) {
            throw new DaoException("Error loading all courses", e);
        }
    }

    /**
     * Finds course by its exact name.
     *
     * @param name course name
     * @return {@link Course} or {@code null}, якщо не знайдено або name порожнє
     * @throws DaoException if a database access error occurs
     */
    @Override
    public Course findByName(String name) {
        if (name == null || name.isBlank()) {
            return null;
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
            throw new DaoException("Error finding course by name=" + name, e);
        }
    }

    /**
     * Returns all courses taught by a given teacher.
     *
     * @param teacherId teacher identifier
     * @return list of courses (empty list if {@code teacherId} is {@code null} or нічого не знайдено)
     * @throws DaoException if a database access error occurs
     */
    @Override
    public List<Course> findByTeacherId(Long teacherId) {
        List<Course> result = new ArrayList<>();
        if (teacherId == null) {
            return result;
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_TEACHER_SQL)) {

            ps.setLong(1, teacherId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }

            return result;
        } catch (SQLException e) {
            throw new DaoException("Error finding courses for teacherId=" + teacherId, e);
        }
    }

    /**
     * Inserts a new course row into the database.
     *
     * <p>Після успішної вставки встановлює id в обʼєкті {@code course}.
     *
     * @param course non-null course to insert
     * @return the same {@link Course} instance with populated id
     * @throws IllegalArgumentException if {@code course} is {@code null}
     * @throws DaoException             if insert fails or DB error occurs
     */
    @Override
    public Course insert(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("course must not be null");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, course.getName());
            ps.setShort(2, course.getSemester());
            ps.setShort(3, course.getYear());

            if (course.getTeacherId() != null) {
                ps.setLong(4, course.getTeacherId());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DaoException("Inserting course failed, no rows affected: " + course);
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    course.setId(id);
                } else {
                    throw new DaoException("Inserting course failed, no ID obtained: " + course);
                }
            }

            return course;
        } catch (SQLException e) {
            throw new DaoException("Error inserting course " + course, e);
        }
    }

    /**
     * Updates an existing course row.
     *
     * @param course course with non-null id and новими даними
     * @return {@code true} if at least one row was updated, {@code false} if id not found
     * @throws IllegalArgumentException if course or its id is {@code null}
     * @throws DaoException             if a database access error occurs
     */
    @Override
    public boolean update(Course course) {
        if (course == null || course.getId() == null) {
            throw new IllegalArgumentException("course and course.id must not be null");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, course.getName());
            ps.setShort(2, course.getSemester());
            ps.setShort(3, course.getYear());

            if (course.getTeacherId() != null) {
                ps.setLong(4, course.getTeacherId());
            } else {
                ps.setNull(4, java.sql.Types.BIGINT);
            }

            ps.setLong(5, course.getId());

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating course " + course, e);
        }
    }

    /**
     * Deletes course by id.
     *
     * @param id course identifier
     * @return {@code true} if a row was deleted, {@code false} if id був {@code null} або не знайдено
     * @throws DaoException if a database access error occurs
     */
    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE_SQL)) {

            ps.setLong(1, id);

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting course id=" + id, e);
        }
    }

    /**
     * Maps current row of {@link ResultSet} to a {@link Course} instance.
     *
     * @param rs result set positioned at a valid row
     * @return mapped {@link Course}
     * @throws SQLException if column access fails
     */
    private Course mapRow(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getLong("id"));
        course.setName(rs.getString("name"));
        course.setSemester(rs.getShort("semester"));
        course.setYear(rs.getShort("year"));

        long teacherId = rs.getLong("teacher_id");
        if (rs.wasNull()) {
            course.setTeacherId(null);
        } else {
            course.setTeacherId(teacherId);
        }

        return course;
    }
}
