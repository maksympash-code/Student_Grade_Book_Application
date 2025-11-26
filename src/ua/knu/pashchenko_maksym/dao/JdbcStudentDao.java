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

/**
 * JDBC-реалізація {@link StudentDao} для таблиці {@code students}.
 *
 * <p>Інкапсулює типові операції по роботі зі студентами:
 * створення, читання, оновлення, видалення, а також пошук за групою
 * та курсом. Працює напряму через {@link java.sql.Connection} /
 * {@link PreparedStatement} без використання ORM.
 *
 * <p>Очікується, що структура таблиці {@code students}
 * відповідає вибраним колонкам у {@link #SELECT_BASE}.
 *
 * @author Pashchenko Maksym
 * @since 26.11.2025
 */
public class JdbcStudentDao implements StudentDao {

    /**
     * Базовий SELECT для всіх запитів по студентам.
     */
    private static final String SELECT_BASE =
            "SELECT id, first_name, last_name, email, group_id, "
                    + "enrollment_year, created_at FROM students ";

    /**
     * Пошук студента за id.
     */
    private static final String SELECT_BY_ID =
            SELECT_BASE + "WHERE id = ?";

    /**
     * Отримання всіх студентів.
     */
    private static final String SELECT_ALL =
            SELECT_BASE + "ORDER BY last_name, first_name";

    /**
     * Студенти конкретної академічної групи.
     */
    private static final String SELECT_BY_GROUP =
            SELECT_BASE + "WHERE group_id = ? ORDER BY last_name, first_name";

    /**
     * Студенти, що мають оцінки з конкретного курсу.
     */
    private static final String SELECT_BY_COURSE =
            "SELECT DISTINCT s.id, s.first_name, s.last_name, s.email, "
                    + "s.group_id, s.enrollment_year, s.created_at "
                    + "FROM students s "
                    + "JOIN grades g ON g.student_id = s.id "
                    + "WHERE g.course_id = ? "
                    + "ORDER BY s.last_name, s.first_name";

    /**
     * Вставка нового студента.
     */
    private static final String INSERT_SQL =
            "INSERT INTO students (first_name, last_name, email, group_id, enrollment_year) "
                    + "VALUES (?, ?, ?, ?, ?) RETURNING id, created_at";

    /**
     * Оновлення існуючого студента.
     */
    private static final String UPDATE_SQL =
            "UPDATE students SET first_name = ?, last_name = ?, email = ?, "
                    + "group_id = ?, enrollment_year = ? WHERE id = ?";

    /**
     * Видалення студента за id.
     */
    private static final String DELETE_SQL =
            "DELETE FROM students WHERE id = ?";

    /**
     * Знаходить студента за первинним ключем.
     *
     * @param id ідентифікатор студента
     * @return знайдений {@link Student} або {@code null}, якщо студента не знайдено
     * @throws DaoException у разі помилки доступу до БД
     */
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

    /**
     * Повертає повний список усіх студентів.
     *
     * @return список студентів (може бути порожнім, але не {@code null})
     * @throws DaoException у разі помилки доступу до БД
     */
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

    /**
     * Додає нового студента до таблиці {@code students}.
     *
     * <p>Після успішної вставки у переданий об'єкт записуються:
     * <ul>
     *     <li>згенерований {@code id}</li>
     *     <li>значення поля {@code created_at}</li>
     * </ul>
     *
     * @param student об'єкт {@link Student}, що зберігається
     * @return той самий екземпляр {@link Student} з оновленими полями id/createdAt
     * @throws DaoException у разі помилки доступу до БД
     */
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

    /**
     * Оновлює дані про студента.
     *
     * @param student об'єкт з оновленими полями (id має бути заповнений)
     * @return {@code true}, якщо було оновлено хоча б один рядок; {@code false}, якщо id не знайдено
     * @throws IllegalArgumentException якщо {@code student.getId() == null}
     * @throws DaoException             у разі помилки доступу до БД
     */
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

    /**
     * Видаляє студента з таблиці за id.
     *
     * @param id ідентифікатор студента
     * @return {@code true}, якщо студент був видалений;
     *         {@code false}, якщо запис не знайдений
     * @throws DaoException у разі помилки доступу до БД
     */
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

    /**
     * Повертає список студентів певної групи.
     *
     * @param groupId ідентифікатор групи
     * @return список студентів цієї групи (може бути порожнім)
     * @throws DaoException у разі помилки доступу до БД
     */
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

    /**
     * Повертає список студентів, які мають оцінки з певного курсу.
     *
     * @param courseId ідентифікатор курсу
     * @return список студентів (може бути порожнім)
     * @throws DaoException у разі помилки доступу до БД
     */
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

    /**
     * Мапить поточний рядок {@link ResultSet} на об'єкт {@link Student}.
     *
     * @param rs результат SQL-запиту, позиціонований на потрібному рядку
     * @return заповнений {@link Student}
     * @throws SQLException у разі помилки читання даних з {@link ResultSet}
     */
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
