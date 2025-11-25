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
import ua.knu.pashchenko_maksym.service.GradeBookService;
import ua.knu.pashchenko_maksym.service.ReportService;
import ua.knu.pashchenko_maksym.util.IoUtil;

/**
 * Application entry point.
 */
public class Main {

    private static final Path TEST_FILE =
            Path.of("resources/testdata/NZ_test.txt");
    private static final Path OUTPUT_TEXT_FILE =
            Path.of("resources/output/result.txt");


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
     * Simple test runner that reads commands from NZ_test.txt and writes summary to result.txt.
     *
     * Формат рядків у NZ_test.txt (приклад):
     *
     * ADD_GROUP;IP-11;1
     * ADD_STUDENT;Maksym;Pashchenko;maks@example.com;1;2024
     * ADD_TEACHER;Ivan;Ivanenko;CS;ivan@example.com
     * ADD_COURSE;Programming 1;1;1;1;4
     * SET_GRADE;1;1;1;95.5;2024-10-01
     * REPORT_STUDENT;1
     * REPORT_GROUP_COURSE;1;1
     * REPORT_TEACHER;1
     *
     * Порожні рядки та рядки, що починаються з #, ігноруються.
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
}
