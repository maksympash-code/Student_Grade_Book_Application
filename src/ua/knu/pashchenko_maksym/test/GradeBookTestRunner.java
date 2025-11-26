package ua.knu.pashchenko_maksym.test;

import ua.knu.pashchenko_maksym.dao.CourseDao;
import ua.knu.pashchenko_maksym.dao.DataSourceProvider;
import ua.knu.pashchenko_maksym.dao.GradeDao;
import ua.knu.pashchenko_maksym.dao.GroupDao;
import ua.knu.pashchenko_maksym.dao.StudentDao;
import ua.knu.pashchenko_maksym.dao.TeacherDao;
import ua.knu.pashchenko_maksym.dao.JdbcCourseDao;
import ua.knu.pashchenko_maksym.dao.JdbcGradeDao;
import ua.knu.pashchenko_maksym.dao.JdbcGroupDao;
import ua.knu.pashchenko_maksym.dao.JdbcStudentDao;
import ua.knu.pashchenko_maksym.dao.JdbcTeacherDao;

import ua.knu.pashchenko_maksym.model.Course;
import ua.knu.pashchenko_maksym.model.Grade;
import ua.knu.pashchenko_maksym.model.Group;
import ua.knu.pashchenko_maksym.model.Student;
import ua.knu.pashchenko_maksym.model.Teacher;

import ua.knu.pashchenko_maksym.service.GradeBookService;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * Простий тестовий раннер для перевірки GradeBookService без JUnit.
 * Запускається як звичайний main-клас.
 */
public class GradeBookTestRunner {

    public static void main(String[] args) {
        System.out.println("===================================");
        System.out.println("     GradeBookService TEST RUN     ");
        System.out.println("===================================");

        try {
            resetDatabase();

            GradeBookService service = buildService();

            // 1. Тестові дані
            Group group = service.createGroup("TEST-11", (short) 1);

            Teacher teacher = service.createTeacher(
                    "Test",
                    "Teacher",
                    "Test Department",
                    "teacher@example.com"
            );

            Course course = service.createCourse(
                    "Test Course",
                    1,          // semester (Integer)
                    2024,       // year (Integer)
                    teacher.getId(),
                    5           // credits (Integer) або null, якщо не потрібно
            );

            Student student = service.createStudent(
                    "Test",
                    "Student",
                    "student@example.com",
                    group.getId(),
                    2024        // enrollmentYear (int)
            );

            // Дві оцінки: 90 і 75 → середнє 82.5
            service.addGrade(
                    student.getId(),
                    course.getId(),
                    teacher.getId(),
                    90.0,
                    LocalDate.of(2024, 1, 10)
            );
            service.addGrade(
                    student.getId(),
                    course.getId(),
                    teacher.getId(),
                    75.0,
                    LocalDate.of(2024, 1, 20)
            );

            // 2. Тести

            System.out.println("\n--- TEST 1: середній бал студента ---");
            double expectedStudentAvg = 82.5;
            double actualStudentAvg = service.getStudentAverageGrade(student.getId());
            assertDouble("Student average grade", expectedStudentAvg, actualStudentAvg);

            System.out.println("\n--- TEST 2: середній бал групи по курсу ---");
            double expectedGroupCourseAvg = 82.5; // тільки один студент з цим курсом
            double actualGroupCourseAvg =
                    service.getGroupAverageForCourse(group.getId(), course.getId());
            assertDouble("Group-course average", expectedGroupCourseAvg, actualGroupCourseAvg);

            System.out.println("\n--- TEST 3: середній бал викладача ---");
            double expectedTeacherAvg = 82.5; // ті самі оцінки від цього викладача
            double actualTeacherAvg =
                    service.getTeacherAverageGrade(teacher.getId());
            assertDouble("Teacher average grade", expectedTeacherAvg, actualTeacherAvg);

            System.out.println("\n--- TEST 4: кількість оцінок студента ---");
            List<Grade> gradesForStudent = service.getGradesForStudent(student.getId());
            int expectedCount = 2;
            int actualCount = gradesForStudent.size();
            if (actualCount == expectedCount) {
                System.out.printf("Grades count OK: expected %d, actual %d%n",
                        expectedCount, actualCount);
            } else {
                System.out.printf("Grades count FAIL: expected %d, actual %d%n",
                        expectedCount, actualCount);
            }

            System.out.println("\n===================================");
            System.out.println("  ALL SIMPLE TESTS FINISHED.      ");
            System.out.println("  Перевір результати вище ↑       ");
            System.out.println("===================================");

        } catch (Exception e) {
            System.out.println("Тест-раннер впав з виключенням:");
            e.printStackTrace();
        }
    }

    /**
     * Очищення даних у БД, щоб тести завжди стартували з чистого стану.
     */
    private static void resetDatabase() {
        try (Connection connection = DataSourceProvider.getConnection();
             Statement st = connection.createStatement()) {

            // порядок важливий, але CASCADE все прибере за нас
            st.executeUpdate("TRUNCATE TABLE grades CASCADE");
            st.executeUpdate("TRUNCATE TABLE courses CASCADE");
            st.executeUpdate("TRUNCATE TABLE students CASCADE");
            st.executeUpdate("TRUNCATE TABLE teachers CASCADE");
            st.executeUpdate("TRUNCATE TABLE groups CASCADE");

            System.out.println("Database truncated for tests.");

        } catch (SQLException e) {
            throw new RuntimeException("Error resetting database before tests", e);
        }
    }

    /**
     * Створює GradeBookService з JDBC-реалізаціями DAO.
     */
    private static GradeBookService buildService() {
        StudentDao studentDao = new JdbcStudentDao();
        GroupDao groupDao = new JdbcGroupDao();
        CourseDao courseDao = new JdbcCourseDao();
        TeacherDao teacherDao = new JdbcTeacherDao();
        GradeDao gradeDao = new JdbcGradeDao();

        return new GradeBookService(
                studentDao,
                groupDao,
                courseDao,
                teacherDao,
                gradeDao
        );
    }

    /**
     * Проста перевірка double з допуском.
     */
    private static void assertDouble(String testName, double expected, double actual) {
        double eps = 1e-3;
        if (Double.isNaN(actual)) {
            System.out.printf("%s: expected %.2f, but was NaN%n", testName, expected);
            return;
        }
        if (Math.abs(expected - actual) < eps) {
            System.out.printf("%s OK: expected %.2f, actual %.2f%n",
                    testName, expected, actual);
        } else {
            System.out.printf("%s FAIL: expected %.2f, actual %.2f%n",
                    testName, expected, actual);
        }
    }
}
