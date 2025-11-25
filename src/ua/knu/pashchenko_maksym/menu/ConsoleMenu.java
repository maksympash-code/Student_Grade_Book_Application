package ua.knu.pashchenko_maksym.menu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import ua.knu.pashchenko_maksym.model.Course;
import ua.knu.pashchenko_maksym.model.Group;
import ua.knu.pashchenko_maksym.model.Student;
import ua.knu.pashchenko_maksym.model.Teacher;
import ua.knu.pashchenko_maksym.service.GradeBookService;
import ua.knu.pashchenko_maksym.service.ReportService;
import ua.knu.pashchenko_maksym.util.IoUtil;

/**
 * Simple console menu to interact with grade book.
 */
public class ConsoleMenu {

    private final GradeBookService gradeBookService;
    private final ReportService reportService;

    private static final Path OUTPUT_DIR =
            Path.of("src/main/resources/output");

    public ConsoleMenu(GradeBookService gradeBookService,
                       ReportService reportService) {
        this.gradeBookService = gradeBookService;
        this.reportService = reportService;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = IoUtil.readIntInRange("Ваш вибір: ", 0, 9);
            System.out.println();

            switch (choice) {
                case 1:
                    listStudents();
                    break;
                case 2:
                    addStudent();
                    break;
                case 3:
                    addGrade();
                    break;
                case 4:
                    showStudentReport();
                    break;
                case 5:
                    showGroupCourseReport();
                    break;
                case 6:
                    showTeacherReport();
                    break;
                case 7:
                    exportStudentGrades();
                    break;
                case 8:
                    exportGroupCourseGrades();
                    break;
                case 9:
                    exportTeacherGrades();
                    break;
                case 0:
                    running = false;
                    System.out.println("До побачення!");
                    break;
                default:
                    System.out.println("Невірний вибір.");
            }

            System.out.println();
        }
    }

    private void printMenu() {
        System.out.println("===================================");
        System.out.println("      Student Grade Book Menu      ");
        System.out.println("===================================");
        System.out.println("1 - Показати всіх студентів");
        System.out.println("2 - Додати студента");
        System.out.println("3 - Додати оцінку");
        System.out.println("4 - Звіт по студенту");
        System.out.println("5 - Звіт по групі та курсу");
        System.out.println("6 - Звіт по викладачу");
        System.out.println("7 - Експорт оцінок студента в CSV");
        System.out.println("8 - Експорт оцінок групи/курсу в CSV");
        System.out.println("9 - Експорт оцінок викладача в CSV");
        System.out.println("0 - Вихід");
    }

    // ============================
    // Menu actions
    // ============================

    private void listStudents() {
        List<Student> students = gradeBookService.getAllStudents();
        if (students.isEmpty()) {
            System.out.println("Студентів поки немає.");
            return;
        }
        System.out.println("=== Список студентів ===");
        for (Student s : students) {
            System.out.printf("%d: %s %s (groupId=%s)%n",
                    s.getId(),
                    s.getFirstName(),
                    s.getLastName(),
                    s.getGroupId());
        }
    }

    private void addStudent() {
        System.out.println("=== Додати студента ===");
        String firstName = IoUtil.readNonEmptyLine("Ім'я: ");
        String lastName = IoUtil.readNonEmptyLine("Прізвище: ");
        String email = IoUtil.readLine("Email (можна пусто): ").trim();
        if (email.isEmpty()) {
            email = null;
        }

        System.out.println("Доступні групи:");
        List<Group> groups = gradeBookService.getAllGroups();
        for (Group g : groups) {
            System.out.printf("  %d: %s (year=%s)%n",
                    g.getId(), g.getName(), g.getYear());
        }

        long groupIdRaw = IoUtil.readLong("ID групи (0 якщо без групи): ");
        Long groupId = groupIdRaw == 0 ? null : groupIdRaw;

        int enrollmentYear = IoUtil.readInt("Рік вступу (наприклад, 2024): ");

        Student created = gradeBookService.createStudent(
                firstName, lastName, email, groupId, enrollmentYear);

        System.out.println("Студента створено: " + created);
    }

    private void addGrade() {
        System.out.println("=== Додати оцінку ===");

        long studentId = IoUtil.readLong("ID студента: ");
        long courseId = IoUtil.readLong("ID курсу: ");
        long teacherIdRaw = IoUtil.readLong("ID викладача (0 якщо невідомий): ");
        Long teacherId = teacherIdRaw == 0 ? null : teacherIdRaw;

        double value = IoUtil.readDouble("Оцінка (0-100): ");
        String dateStr = IoUtil.readLine("Дата (YYYY-MM-DD, пусто = сьогодні): ").trim();

        java.time.LocalDate date = null;
        if (!dateStr.isEmpty()) {
            date = java.time.LocalDate.parse(dateStr);
        }

        try {
            var grade = gradeBookService.addGrade(studentId, courseId, teacherId, value, date);
            System.out.println("Оцінку додано: " + grade);
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        }
    }

    private void showStudentReport() {
        System.out.println("=== Звіт по студенту ===");
        long studentId = IoUtil.readLong("ID студента: ");
        reportService.printStudentReport(studentId);
    }

    private void showGroupCourseReport() {
        System.out.println("=== Звіт по групі та курсу ===");
        long groupId = IoUtil.readLong("ID групи: ");
        long courseId = IoUtil.readLong("ID курсу: ");
        reportService.printGroupCourseReport(groupId, courseId);
    }

    private void showTeacherReport() {
        System.out.println("=== Звіт по викладачу ===");
        long teacherId = IoUtil.readLong("ID викладача: ");
        reportService.printTeacherReport(teacherId);
    }

    // ============================
    // CSV export helpers
    // ============================

    private void exportStudentGrades() {
        System.out.println("=== Експорт оцінок студента ===");
        long studentId = IoUtil.readLong("ID студента: ");
        try {
            Files.createDirectories(OUTPUT_DIR);
            Path file = OUTPUT_DIR.resolve("student_" + studentId + "_grades.csv");
            reportService.exportStudentGradesToCsv(studentId, file);
            System.out.println("Експортовано у файл: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Помилка запису у файл: " + e.getMessage());
        }
    }

    private void exportGroupCourseGrades() {
        System.out.println("=== Експорт оцінок групи/курсу ===");
        long groupId = IoUtil.readLong("ID групи: ");
        long courseId = IoUtil.readLong("ID курсу: ");
        try {
            Files.createDirectories(OUTPUT_DIR);
            Path file = OUTPUT_DIR.resolve("group_" + groupId
                    + "_course_" + courseId + "_grades.csv");
            reportService.exportGroupCourseGradesToCsv(groupId, courseId, file);
            System.out.println("Експортовано у файл: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Помилка запису у файл: " + e.getMessage());
        }
    }

    private void exportTeacherGrades() {
        System.out.println("=== Експорт оцінок викладача ===");
        long teacherId = IoUtil.readLong("ID викладача: ");
        try {
            Files.createDirectories(OUTPUT_DIR);
            Path file = OUTPUT_DIR.resolve("teacher_" + teacherId + "_grades.csv");
            reportService.exportTeacherGradesToCsv(teacherId, file);
            System.out.println("Експортовано у файл: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Помилка запису у файл: " + e.getMessage());
        }
    }
}
