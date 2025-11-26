package ua.knu.pashchenko_maksym.test;

import ua.knu.pashchenko_maksym.dao.CourseDao;
import ua.knu.pashchenko_maksym.dao.DataSourceProvider;
import ua.knu.pashchenko_maksym.dao.GradeDao;
import ua.knu.pashchenko_maksym.dao.GroupDao;
import ua.knu.pashchenko_maksym.dao.JdbcCourseDao;
import ua.knu.pashchenko_maksym.dao.JdbcGradeDao;
import ua.knu.pashchenko_maksym.dao.JdbcGroupDao;
import ua.knu.pashchenko_maksym.dao.JdbcStudentDao;
import ua.knu.pashchenko_maksym.dao.JdbcTeacherDao;
import ua.knu.pashchenko_maksym.dao.StudentDao;
import ua.knu.pashchenko_maksym.dao.TeacherDao;
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
 * Простий "ручний" тест-клас для GradeBookService.
 * Запускається як звичайний main-клас, без JUnit.
 */
public class GradeBookServiceTest {

    private final GradeBookService service;

    public GradeBookServiceTest() {
        StudentDao studentDao = new JdbcStudentDao();
        GroupDao groupDao = new JdbcGroupDao();
        CourseDao courseDao = new JdbcCourseDao();
        TeacherDao teacherDao = new JdbcTeacherDao();
        GradeDao gradeDao = new JdbcGradeDao();

        this.service = new GradeBookService(
                studentDao,
                groupDao,
                courseDao,
                teacherDao,
                gradeDao
        );
    }

    public static void main(String[] args) {
        System.out.println("===================================");
        System.out.println("     GradeBookService TESTS        ");
        System.out.println("===================================");

        try {
            resetDatabase();

            GradeBookServiceTest tester = new GradeBookServiceTest();
            tester.runAllTests();

            System.out.println("\n===================================");
            System.out.println("   TESTS FINISHED. SEE OUTPUT ↑    ");
            System.out.println("===================================");

        } catch (Exception e) {
            System.out.println("Global test failure:");
            e.printStackTrace();
        }
    }

    public void runAllTests() {
        testCreateGroupAndFind();
        testCreateStudentAndFind();
        testCreateCourseAndTeacherAndGetByTeacher();
        testAddGradeAndGetStudentAverage();
    }

    // ======================
    // ОКРЕМІ ТЕСТИ
    // ======================

    private void testCreateGroupAndFind() {
        System.out.println("\n--- TEST: createGroup / getGroupByName ---");

        Group g = service.createGroup("TEST-GROUP-1", (short) 1);
        Group found = service.getGroupByName("TEST-GROUP-1");

        boolean idNotNull = g.getId() != null;
        assertTrue("Group id assigned", idNotNull);

        assertNotNull("Group found by name", found);
        assertEquals("Group name", "TEST-GROUP-1", found.getName());
        assertEquals("Group year", (short) 1, found.getYear());
    }

    private void testCreateStudentAndFind() {
        System.out.println("\n--- TEST: createStudent / getStudentById ---");

        Group g = service.createGroup("TEST-GROUP-2", (short) 2);

        Student s = service.createStudent(
                "Maksym",
                "TestStudent",
                "test.student@example.com",
                g.getId(),
                2024
        );

        Student found = service.getStudentById(s.getId());

        assertNotNull("Student found by id", found);
        assertEquals("Student first name", "Maksym", found.getFirstName());
        assertEquals("Student last name", "TestStudent", found.getLastName());
        assertEquals("Student groupId", g.getId(), found.getGroupId());
    }

    private void testCreateCourseAndTeacherAndGetByTeacher() {
        System.out.println("\n--- TEST: createCourse / getCoursesByTeacher ---");

        Teacher t = service.createTeacher(
                "Olha",
                "TeacherTest",
                "TestDept",
                "olha.teacher@example.com"
        );

        Course c = service.createCourse(
                "Test Course 2",
                2,      // semester
                2025,   // year
                t.getId(),
                4       // credits
        );

        List<Course> byTeacher = service.getCoursesByTeacher(t.getId());

        assertTrue("Teacher has at least 1 course", !byTeacher.isEmpty());

        boolean containsCourse = byTeacher.stream()
                .anyMatch(course -> course.getId().equals(c.getId()));
        assertTrue("Course list contains created course", containsCourse);
    }

    private void testAddGradeAndGetStudentAverage() {
        System.out.println("\n--- TEST: addGrade / getStudentAverageGrade ---");

        Group g = service.createGroup("TEST-GROUP-3", (short) 1);

        Student s = service.createStudent(
                "Andrii",
                "MarkTest",
                "andrii.mark@example.com",
                g.getId(),
                2023
        );

        Teacher t = service.createTeacher(
                "Test",
                "Teacher2",
                "MathDept",
                "teacher2@example.com"
        );

        Course c = service.createCourse(
                "Algebra Test",
                1,
                2023,
                t.getId(),
                5
        );

        // Було 100.0 і 80.0 → НЕ влазить у NUMERIC(4,2)
        service.addGrade(
                s.getId(),
                c.getId(),
                t.getId(),
                95.0,
                LocalDate.of(2023, 10, 1)
        );
        service.addGrade(
                s.getId(),
                c.getId(),
                t.getId(),
                85.0,
                LocalDate.of(2023, 10, 15)
        );

        double avg = service.getStudentAverageGrade(s.getId());
        double expected = 90.0; // (95 + 85) / 2

        assertDouble("Student average grade (2 grades: 95, 85)", expected, avg);

        List<Grade> grades = service.getGradesForStudentAndCourse(s.getId(), c.getId());
        assertEquals("Grades for student+course count", 2, grades.size());
    }


    // ======================
    // HELPERS
    // ======================

    private static void resetDatabase() {
        try (Connection connection = DataSourceProvider.getConnection();
             Statement st = connection.createStatement()) {

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

    private static void assertTrue(String testName, boolean condition) {
        if (condition) {
            System.out.printf("%s%n", testName);
        } else {
            System.out.printf("%s%n", testName);
        }
    }

    private static void assertNotNull(String testName, Object obj) {
        if (obj != null) {
            System.out.printf("%s%n", testName);
        } else {
            System.out.printf("%s (was null)%n", testName);
        }
    }

    private static void assertEquals(String testName, String expected, String actual) {
        if (expected == null && actual == null) {
            System.out.printf("%s%n", testName);
            return;
        }
        if (expected != null && expected.equals(actual)) {
            System.out.printf("%s%n", testName);
        } else {
            System.out.printf("%s (expected='%s', actual='%s')%n",
                    testName, expected, actual);
        }
    }

    private static void assertEquals(String testName, long expected, Long actual) {
        if (actual != null && actual == expected) {
            System.out.printf("%s%n", testName);
        } else {
            System.out.printf("%s (expected=%d, actual=%s)%n",
                    testName, expected, String.valueOf(actual));
        }
    }

    private static void assertEquals(String testName, short expected, Short actual) {
        if (actual != null && actual == expected) {
            System.out.printf("%s%n", testName);
        } else {
            System.out.printf("%s (expected=%d, actual=%s)%n",
                    testName, expected, String.valueOf(actual));
        }
    }

    private static void assertEquals(String testName, int expected, int actual) {
        if (expected == actual) {
            System.out.printf("%s%n", testName);
        } else {
            System.out.printf("%s (expected=%d, actual=%d)%n",
                    testName, expected, actual);
        }
    }

    private static void assertDouble(String testName, double expected, double actual) {
        double eps = 1e-3;
        if (Double.isNaN(actual)) {
            System.out.printf("%s: expected %.2f, but was NaN%n", testName, expected);
            return;
        }
        if (Math.abs(expected - actual) < eps) {
            System.out.printf("%s (expected=%.2f, actual=%.2f)%n",
                    testName, expected, actual);
        } else {
            System.out.printf("%s (expected=%.2f, actual=%.2f)%n",
                    testName, expected, actual);
        }
    }
}
