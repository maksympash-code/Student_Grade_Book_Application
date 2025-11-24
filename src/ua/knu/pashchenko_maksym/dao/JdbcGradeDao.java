package ua.knu.pashchenko_maksym.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import ua.knu.pashchenko_maksym.model.Grade;

public class JdbcGradeDao implements GradeDao {

    private static final String SELECT_BASE =
            "SELECT id, student_id, course_id, teacher_id, value, grade_date FROM grades ";

    private static final String SELECT_BY_ID =
            SELECT_BASE + "WHERE id = ?";

    private static final String SELECT_ALL =
            SELECT_BASE + "ORDER BY grade_date DESC, id";

    private static final String SELECT_BY_STUDENT =
            SELECT_BASE + "WHERE student_id = ? ORDER BY grade_date DESC, id";

    private static final String SELECT_BY_COURSE =
            SELECT_BASE + "WHERE course_id = ? ORDER BY grade_date DESC, id";

    private static final String SELECT_BY_TEACHER =
            SELECT_BASE + "WHERE teacher_id = ? ORDER BY grade_date DESC, id";

    private static final String SELECT_BY_STUDENT_COURSE =
            SELECT_BASE + "WHERE student_id = ? AND course_id = ? "
                    + "ORDER BY grade_date DESC, id";

    private static final String INSERT_SQL =
            "INSERT INTO grades (student_id, course_id, teacher_id, value, grade_date) "
                    + "VALUES (?, ?, ?, ?, ?) RETURNING id";

    private static final String UPDATE_SQL =
            "UPDATE grades SET student_id = ?, course_id = ?, teacher_id = ?, "
                    + "value = ?, grade_date = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM grades WHERE id = ?";

    @Override
    public Grade findById(Long id) {
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
            throw new DaoException("Error finding grade by id " + id, e);
        }
    }

    @Override
    public List<Grade> findAll() {
        List<Grade> result = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new DaoException("Error loading all grades", e);
        }
    }

    @Override
    public Grade insert(Grade grade) {
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {

            ps.setLong(1, grade.getStudentId());
            ps.setLong(2, grade.getCourseId());

            if (grade.getTeacherId() != null) {
                ps.setLong(3, grade.getTeacherId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            BigDecimal value = grade.getValue();
            if (value == null) {
                throw new IllegalArgumentException("Grade value must not be null");
            }
            ps.setBigDecimal(4, value);

            LocalDate date = grade.getGradeDate();
            if (date == null) {
                date = LocalDate.now();
                grade.setGradeDate(date);
            }
            ps.setDate(5, Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    grade.setId(id);
                }
            }

            return grade;

        } catch (SQLException e) {
            throw new DaoException("Error inserting grade " + grade, e);
        }
    }

    @Override
    public boolean update(Grade grade) {
        if (grade.getId() == null) {
            throw new IllegalArgumentException("Grade id must not be null for update");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setLong(1, grade.getStudentId());
            ps.setLong(2, grade.getCourseId());

            if (grade.getTeacherId() != null) {
                ps.setLong(3, grade.getTeacherId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }

            if (grade.getValue() == null) {
                throw new IllegalArgumentException("Grade value must not be null");
            }
            ps.setBigDecimal(4, grade.getValue());

            LocalDate date = grade.getGradeDate();
            if (date == null) {
                date = LocalDate.now();
                grade.setGradeDate(date);
            }
            ps.setDate(5, Date.valueOf(date));

            ps.setLong(6, grade.getId());

            int updated = ps.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            throw new DaoException("Error updating grade " + grade, e);
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
            throw new DaoException("Error deleting grade with id " + id, e);
        }
    }

    @Override
    public List<Grade> findByStudentId(Long studentId) {
        List<Grade> result = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_STUDENT)) {

            ps.setLong(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new DaoException("Error finding grades by student " + studentId, e);
        }
    }

    @Override
    public List<Grade> findByCourseId(Long courseId) {
        List<Grade> result = new ArrayList<>();
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
            throw new DaoException("Error finding grades by course " + courseId, e);
        }
    }

    @Override
    public List<Grade> findByTeacherId(Long teacherId) {
        List<Grade> result = new ArrayList<>();
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
            throw new DaoException("Error finding grades by teacher " + teacherId, e);
        }
    }

    @Override
    public List<Grade> findByStudentAndCourse(Long studentId, Long courseId) {
        List<Grade> result = new ArrayList<>();
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_BY_STUDENT_COURSE)) {

            ps.setLong(1, studentId);
            ps.setLong(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;

        } catch (SQLException e) {
            throw new DaoException("Error finding grades by student "
                    + studentId + " and course " + courseId, e);
        }
    }

    private Grade mapRow(ResultSet rs) throws SQLException {
        Grade grade = new Grade();
        grade.setId(rs.getLong("id"));
        grade.setStudentId(rs.getLong("student_id"));
        grade.setCourseId(rs.getLong("course_id"));

        long teacherId = rs.getLong("teacher_id");
        if (!rs.wasNull()) {
            grade.setTeacherId(teacherId);
        }

        grade.setValue(rs.getBigDecimal("value"));

        Date date = rs.getDate("grade_date");
        if (date != null) {
            grade.setGradeDate(date.toLocalDate());
        }

        return grade;
    }
}

