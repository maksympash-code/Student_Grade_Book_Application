package ua.knu.pashchenko_maksym;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import ua.knu.pashchenko_maksym.dao.CourseDao;
import ua.knu.pashchenko_maksym.dao.GradeDao;
import ua.knu.pashchenko_maksym.dao.GroupDao;
import ua.knu.pashchenko_maksym.dao.JdbcCourseDao;
import ua.knu.pashchenko_maksym.dao.JdbcGradeDao;
import ua.knu.pashchenko_maksym.dao.JdbcGroupDao;
import ua.knu.pashchenko_maksym.dao.JdbcStudentDao;
import ua.knu.pashchenko_maksym.dao.JdbcTeacherDao;
import ua.knu.pashchenko_maksym.dao.StudentDao;
import ua.knu.pashchenko_maksym.dao.TeacherDao;
import ua.knu.pashchenko_maksym.menu.ConsoleMenu;
import ua.knu.pashchenko_maksym.model.Course;
import ua.knu.pashchenko_maksym.model.Group;
import ua.knu.pashchenko_maksym.model.Student;
import ua.knu.pashchenko_maksym.model.Teacher;
import ua.knu.pashchenko_maksym.service.GradeBookService;
import ua.knu.pashchenko_maksym.service.ReportService;
import ua.knu.pashchenko_maksym.util.IoUtil;

/**
 * Точка входу в застосунок "Student Grade Book System".
 *
 * <p>Клас відповідає за:
 * <ul>
 *     <li>ініціалізацію DAO-рівня ({@link JdbcStudentDao}, {@link JdbcGroupDao}, {@link JdbcCourseDao},
 *     {@link JdbcTeacherDao}, {@link JdbcGradeDao});</li>
 *     <li>створення сервісів {@link GradeBookService} та {@link ReportService};</li>
 *     <li>запуск у одному з двох режимів:
 *     <ul>
 *         <li>інтерактивний консольний режим ({@link ConsoleMenu});</li>
 *         <li>тестовий режим зчитування команд із текстового файлу NZ_test.txt.</li>
 *     </ul>
 *     </li>
 * </ul>
 *
 * @author Pashchenko Maksym
 * @since 26.11.2025
 */
public class Main {

    /**
     * Шлях до тестового файлу з командами для тестового режиму.
     */
    private static final Path TEST_FILE =
            Path.of("resources/testdata/NZ_test.txt");

    /**
     * Шлях до текстового файлу, куди записується лог виконання тестового сценарію.
     */
    private static final Path OUTPUT_TEXT_FILE =
            Path.of("resources/output/result.txt");

    /**
     * Головний метод застосунку.
     *
     * <p>Ініціалізує всі DAO, створює сервіси
     * {@link GradeBookService} та {@link ReportService}, а потім
     * пропонує користувачу обрати режим запуску:
     * <ul>
     *     <li>1 — інтерактивне консольне меню ({@link ConsoleMenu});</li>
     *     <li>2 — виконання сценарію з файлу {@code NZ_test.txt} ({@link #runTestScript(GradeBookService, ReportService)}).</li>
     * </ul>
     *
     * @param args параметри командного рядка (не використовуються)
     */
    public static void main(String[] args) {
        StudentDao studentDao = new JdbcStudentDao();
        GroupDao groupDao = new JdbcGroupDao();
        CourseDao courseDao = new JdbcCourseDao();
        TeacherDao teacherDao = new JdbcTeacherDao();
        GradeDao gradeDao = new JdbcGradeDao();

        GradeBookService gradeBookService =
                new GradeBookService(studentDao, groupDao, courseDao, teacherDao, gradeDao);

        ReportService reportService =
                new ReportService(studentDao, groupDao, courseDao, teacherDao, gradeDao, gradeBookService);

        System.out.println("===================================");
        System.out.println("     Student Grade Book System     ");
        System.out.println("===================================");
        System.out.println("Оберіть режим запуску:");
        System.out.println("1 - Інтерактивний режим (консоль)");
        System.out.println("2 - Тестовий режим (файл NZ_test.txt)");

        int mode = IoUtil.readIntInRange("Режим (1-2): ", 1, 2);
        System.out.println();

        if (mode == 1) {
            ConsoleMenu menu = new ConsoleMenu(gradeBookService, reportService);
            menu.run();
        } else {
            runTestScript(gradeBookService, reportService);
        }
    }

    /**
     * Простий тестовий запускач, який читає команди з NZ_test.txt
     * та записує короткий текстовий лог у result.txt.
     *
     * <p>Формат рядків у NZ_test.txt (приклад):
     *
     * <pre>
     * ADD_GROUP;IP-11;1
     * ADD_STUDENT;Maksym;Pashchenko;maks@example.com;1;2024
     * ADD_TEACHER;Ivan;Ivanenko;CS;ivan@example.com
     * ADD_COURSE;Programming 1;1;1;1;4
     * SET_GRADE;1;1;1;95.5;2024-10-01
     * REPORT_STUDENT;1
     * REPORT_GROUP_COURSE;1;1
     * REPORT_TEACHER;1
     * </pre>
     *
     * Порожні рядки та рядки, що починаються з {@code #}, ігноруються.
     *
     * @param gradeBookService сервіс для CRUD-операцій і розрахунків
     * @param reportService    сервіс для формування звітів
     */
    private static void runTestScript(GradeBookService gradeBookService,
                                      ReportService reportService) {
        System.out.println("=== Тестовий режим ===");

        StringBuilder log = new StringBuilder();
        log.append("=== Test run log ===\n");

        try {
            List<String> lines = Files.readAllLines(TEST_FILE, StandardCharsets.UTF_8);

            for (String rawLine : lines) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(";");
                String cmd = parts[0].trim().toUpperCase();

                log.append("Command: ").append(line).append('\n');

                switch (cmd) {
                    case "ADD_GROUP":
                        handleAddGroup(parts, gradeBookService, log);
                        break;
                    case "ADD_STUDENT":
                        handleAddStudent(parts, gradeBookService, log);
                        break;
                    case "ADD_TEACHER":
                        handleAddTeacher(parts, gradeBookService, log);
                        break;
                    case "ADD_COURSE":
                        handleAddCourse(parts, gradeBookService, log);
                        break;
                    case "SET_GRADE":
                        handleSetGrade(parts, gradeBookService, log);
                        break;
                    case "REPORT_STUDENT":
                        handleReportStudent(parts, reportService, gradeBookService, log);
                        break;
                    case "REPORT_GROUP_COURSE":
                        handleReportGroupCourse(parts, reportService, gradeBookService, log);
                        break;
                    case "REPORT_TEACHER":
                        handleReportTeacher(parts, reportService, gradeBookService, log);
                        break;
                    default:
                        log.append("  Unknown command: ").append(cmd).append('\n');
                }

                log.append('\n');
            }

            Files.createDirectories(OUTPUT_TEXT_FILE.getParent());
            Files.writeString(OUTPUT_TEXT_FILE, log.toString(), StandardCharsets.UTF_8);

            System.out.println("Тестовий сценарій виконано.");
            System.out.println("Результат записано у: " + OUTPUT_TEXT_FILE.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Помилка читання/запису тестових файлів: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Помилка під час виконання тестового сценарію: " + e.getMessage());
        }
    }

    // ============================
    // Test script handlers
    // ============================

    /**
     * Обробляє команду {@code ADD_GROUP} із тестового файлу.
     *
     * <p>Очікуваний формат:
     * {@code ADD_GROUP;name;year}
     *
     * @param parts   розбитий по {@code ;} рядок із файлу
     * @param service сервіс журналу оцінок
     * @param log     буфер для запису текстового логу
     */
    private static void handleAddGroup(String[] parts,
                                       GradeBookService service,
                                       StringBuilder log) {
        if (parts.length < 3) {
            log.append("  ERROR: ADD_GROUP requires name;year\n");
            return;
        }
        String name = parts[1].trim();
        int year = Integer.parseInt(parts[2].trim());
        var g = service.createGroup(name, (short) year);
        log.append("  OK: created group ").append(g).append('\n');
    }

    /**
     * Обробляє команду {@code ADD_STUDENT} із тестового файлу.
     *
     * <p>Очікуваний формат:
     * {@code ADD_STUDENT;firstName;lastName;email;groupId;enrollmentYear}
     */
    private static void handleAddStudent(String[] parts,
                                         GradeBookService service,
                                         StringBuilder log) {
        if (parts.length < 6) {
            log.append("  ERROR: ADD_STUDENT requires firstName;lastName;email;groupId;enrollmentYear\n");
            return;
        }
        String firstName = parts[1].trim();
        String lastName = parts[2].trim();
        String email = parts[3].trim();
        long groupId = Long.parseLong(parts[4].trim());
        int year = Integer.parseInt(parts[5].trim());

        var s = service.createStudent(firstName, lastName, email, groupId, year);
        log.append("  OK: created student ").append(s).append('\n');
    }

    /**
     * Обробляє команду {@code ADD_TEACHER} із тестового файлу.
     *
     * <p>Очікуваний формат:
     * {@code ADD_TEACHER;firstName;lastName;department;email}
     */
    private static void handleAddTeacher(String[] parts,
                                         GradeBookService service,
                                         StringBuilder log) {
        if (parts.length < 5) {
            log.append("  ERROR: ADD_TEACHER requires firstName;lastName;department;email\n");
            return;
        }
        String firstName = parts[1].trim();
        String lastName = parts[2].trim();
        String department = parts[3].trim();
        String email = parts[4].trim();

        var t = service.createTeacher(firstName, lastName, department, email);
        log.append("  OK: created teacher ").append(t).append('\n');
    }

    /**
     * Обробляє команду {@code ADD_COURSE} із тестового файлу.
     *
     * <p>Очікуваний формат:
     * {@code ADD_COURSE;name;semester;year;teacherId;credits}
     */
    private static void handleAddCourse(String[] parts,
                                        GradeBookService service,
                                        StringBuilder log) {
        if (parts.length < 6) {
            log.append("  ERROR: ADD_COURSE requires name;semester;year;teacherId;credits\n");
            return;
        }
        String name = parts[1].trim();
        Integer semester = Integer.parseInt(parts[2].trim());
        Integer year = Integer.parseInt(parts[3].trim());
        Long teacherId = Long.parseLong(parts[4].trim());
        Integer credits = Integer.parseInt(parts[5].trim());

        var c = service.createCourse(name, semester, year, teacherId, credits);
        log.append("  OK: created course ").append(c).append('\n');
    }

    /**
     * Обробляє команду {@code SET_GRADE} із тестового файлу.
     *
     * <p>Очікуваний формат:
     * {@code SET_GRADE;studentId;courseId;teacherIdOr0;value;date}
     * (якщо {@code teacherIdOr0 = 0}, оцінка не прив'язується до викладача).
     */
    private static void handleSetGrade(String[] parts,
                                       GradeBookService service,
                                       StringBuilder log) {
        if (parts.length < 6) {
            log.append("  ERROR: SET_GRADE requires studentId;courseId;teacherIdOr0;value;date\n");
            return;
        }
        long studentId = Long.parseLong(parts[1].trim());
        long courseId = Long.parseLong(parts[2].trim());
        long teacherIdRaw = Long.parseLong(parts[3].trim());
        Long teacherId = teacherIdRaw == 0 ? null : teacherIdRaw;
        double value = Double.parseDouble(parts[4].trim());
        String dateStr = parts[5].trim();

        java.time.LocalDate date =
                dateStr.isEmpty() ? java.time.LocalDate.now() : java.time.LocalDate.parse(dateStr);

        try {
            var g = service.addGrade(studentId, courseId, teacherId, value, date);
            log.append("  OK: added grade ").append(g).append('\n');
        } catch (IllegalArgumentException e) {
            log.append("  ERROR: ").append(e.getMessage()).append('\n');
        }
    }

    /**
     * Обробляє команду {@code REPORT_STUDENT}:
     * рахує середній бал студента і формує звіт.
     */
    private static void handleReportStudent(String[] parts,
                                            ReportService reportService,
                                            GradeBookService service,
                                            StringBuilder log) {
        if (parts.length < 2) {
            log.append("  ERROR: REPORT_STUDENT requires studentId\n");
            return;
        }
        long studentId = Long.parseLong(parts[1].trim());
        log.append("  Student average: ")
                .append(String.format("%.2f", service.getStudentAverageGrade(studentId)))
                .append('\n');
        reportService.printStudentReport(studentId);
    }

    /**
     * Обробляє команду {@code REPORT_GROUP_COURSE}:
     * рахує середній бал групи по курсу і формує звіт.
     */
    private static void handleReportGroupCourse(String[] parts,
                                                ReportService reportService,
                                                GradeBookService service,
                                                StringBuilder log) {
        if (parts.length < 3) {
            log.append("  ERROR: REPORT_GROUP_COURSE requires groupId;courseId\n");
            return;
        }
        long groupId = Long.parseLong(parts[1].trim());
        long courseId = Long.parseLong(parts[2].trim());
        double avg = service.getGroupAverageForCourse(groupId, courseId);
        log.append("  Group-course average: ")
                .append(String.format("%.2f", avg))
                .append('\n');
        reportService.printGroupCourseReport(groupId, courseId);
    }

    /**
     * Обробляє команду {@code REPORT_TEACHER}:
     * рахує середній бал оцінок, виставлених викладачем, і формує звіт.
     */
    private static void handleReportTeacher(String[] parts,
                                            ReportService reportService,
                                            GradeBookService service,
                                            StringBuilder log) {
        if (parts.length < 2) {
            log.append("  ERROR: REPORT_TEACHER requires teacherId\n");
            return;
        }
        long teacherId = Long.parseLong(parts[1].trim());
        double avg = service.getTeacherAverageGrade(teacherId);
        log.append("  Teacher average: ")
                .append(String.format("%.2f", avg))
                .append('\n');
        reportService.printTeacherReport(teacherId);
    }

    /**
     * Хендлер редагування студента для інтерактивного режиму
     * (не використовується у тестовому файлі).
     */
    private static void handleEditStudent(GradeBookService service) {
        long id = IoUtil.readLong("ID студента для редагування: ");
        Student student = service.getStudentById(id);
        if (student == null) {
            System.out.println("Студента з таким ID не знайдено.");
            return;
        }

        System.out.println("Поточні дані: " + student);

        String firstName = IoUtil.readNonEmptyLine("Нове ім'я: ");
        String lastName = IoUtil.readNonEmptyLine("Нове прізвище: ");
        String email = IoUtil.readNonEmptyLine("Новий email: ");
        long groupId = IoUtil.readLong("ID групи: ");
        int enrollmentYear = IoUtil.readInt("Рік вступу: ");

        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setGroupId(groupId);
        student.setEnrollmentYear((short) enrollmentYear);

        boolean ok = service.updateStudent(student);
        System.out.println(ok ? "Студента оновлено." : "Помилка оновлення студента.");
    }

    /**
     * Хендлер видалення студента для інтерактивного режиму.
     */
    private static void handleDeleteStudent(GradeBookService service) {
        long id = IoUtil.readLong("ID студента для видалення: ");
        boolean ok = service.deleteStudent(id);
        System.out.println(ok ? "Студента видалено." : "Студента з таким ID не знайдено.");
    }

    /**
     * Хендлер редагування групи для інтерактивного режиму.
     */
    private static void handleEditGroup(GradeBookService service) {
        long id = IoUtil.readLong("ID групи для редагування: ");
        Group group = service.getGroupById(id);
        if (group == null) {
            System.out.println("Групу з таким ID не знайдено.");
            return;
        }

        System.out.println("Поточні дані: " + group);

        String name = IoUtil.readNonEmptyLine("Нова назва групи: ");
        int year = IoUtil.readInt("Новий курс (1-6): ");

        group.setName(name);
        group.setYear((short) year);

        boolean ok = service.updateGroup(group);
        System.out.println(ok ? "Групу оновлено." : "Помилка оновлення групи.");
    }

    /**
     * Хендлер видалення групи для інтерактивного режиму.
     */
    private static void handleDeleteGroup(GradeBookService service) {
        long id = IoUtil.readLong("ID групи для видалення: ");
        boolean ok = service.deleteGroup(id);
        System.out.println(ok ? "Групу видалено." : "Групу з таким ID не знайдено.");
    }

    /**
     * Хендлер редагування курсу для інтерактивного режиму.
     */
    private static void handleEditCourse(GradeBookService service) {
        long id = IoUtil.readLong("ID курсу для редагування: ");
        Course course = service.getCourseById(id);
        if (course == null) {
            System.out.println("Курс з таким ID не знайдено.");
            return;
        }

        System.out.println("Поточні дані: " + course);

        String name = IoUtil.readNonEmptyLine("Нова назва курсу: ");
        int semester = IoUtil.readInt("Семестр (1-2): ");
        int year = IoUtil.readInt("Рік викладання (наприклад 2024): ");
        long teacherId = IoUtil.readLong("ID викладача: ");
        int credits = IoUtil.readInt("Кількість кредитів (ECTS): ");

        course.setName(name);
        course.setSemester((short) semester);
        course.setYear((short) year);
        course.setTeacherId(teacherId);
        course.setCredits((short) credits);

        boolean ok = service.updateCourse(course);
        System.out.println(ok ? "Курс оновлено." : "Помилка оновлення курсу.");
    }

    /**
     * Хендлер видалення курсу для інтерактивного режиму.
     */
    private static void handleDeleteCourse(GradeBookService service) {
        long id = IoUtil.readLong("ID курсу для видалення: ");
        boolean ok = service.deleteCourse(id);
        System.out.println(ok ? "Курс видалено." : "Курс з таким ID не знайдено.");
    }

    /**
     * Хендлер редагування викладача для інтерактивного режиму.
     */
    private static void handleEditTeacher(GradeBookService service) {
        long id = IoUtil.readLong("ID викладача для редагування: ");
        Teacher teacher = service.getTeacherById(id);
        if (teacher == null) {
            System.out.println("Викладача з таким ID не знайдено.");
            return;
        }

        System.out.println("Поточні дані: " + teacher);

        String firstName = IoUtil.readNonEmptyLine("Нове ім'я: ");
        String lastName = IoUtil.readNonEmptyLine("Нове прізвище: ");
        String department = IoUtil.readNonEmptyLine("Новий департамент: ");
        String email = IoUtil.readNonEmptyLine("Новий email: ");

        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setDepartment(department);
        teacher.setEmail(email);

        boolean ok = service.updateTeacher(teacher);
        System.out.println(ok ? "Викладача оновлено." : "Помилка оновлення викладача.");
    }

    /**
     * Хендлер видалення викладача для інтерактивного режиму.
     */
    private static void handleDeleteTeacher(GradeBookService service) {
        long id = IoUtil.readLong("ID викладача для видалення: ");
        boolean ok = service.deleteTeacher(id);
        System.out.println(ok ? "Викладача видалено." : "Викладача з таким ID не знайдено.");
    }

    /**
     * Хендлер для виводу всіх груп (інтерактивний режим).
     */
    private static void handleShowAllGroups(GradeBookService service) {
        List<Group> groups = service.getAllGroups();
        if (groups.isEmpty()) {
            System.out.println("Груп поки немає.");
            return;
        }
        System.out.println("=== Список груп ===");
        for (Group g : groups) {
            System.out.println(g);
        }
    }

    /**
     * Хендлер для виводу всіх курсів (інтерактивний режим).
     */
    private static void handleShowAllCourses(GradeBookService service) {
        List<Course> courses = service.getAllCourses();
        if (courses.isEmpty()) {
            System.out.println("Курсів поки немає.");
            return;
        }
        System.out.println("=== Список курсів ===");
        for (Course c : courses) {
            System.out.println(c);
        }
    }

    /**
     * Хендлер для виводу всіх викладачів (інтерактивний режим).
     */
    private static void handleShowAllTeachers(GradeBookService service) {
        List<Teacher> teachers = service.getAllTeachers();
        if (teachers.isEmpty()) {
            System.out.println("Викладачів поки немає.");
            return;
        }
        System.out.println("=== Список викладачів ===");
        for (Teacher t : teachers) {
            System.out.println(t);
        }
    }
}
