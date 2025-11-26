package ua.knu.pashchenko_maksym.menu;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import ua.knu.pashchenko_maksym.model.Course;
import ua.knu.pashchenko_maksym.model.Grade;
import ua.knu.pashchenko_maksym.model.Group;
import ua.knu.pashchenko_maksym.model.Student;
import ua.knu.pashchenko_maksym.model.Teacher;
import ua.knu.pashchenko_maksym.service.GradeBookService;
import ua.knu.pashchenko_maksym.service.ReportService;
import ua.knu.pashchenko_maksym.util.IoUtil;

/**
 * Консольне меню для взаємодії з системою електронного журналу.
 *
 * <p>Через це меню користувач може:
 * <ul>
 *     <li>створювати, редагувати та видаляти студентів, групи, курси, викладачів;</li>
 *     <li>додавати оцінки;</li>
 *     <li>переглядати звіти по студенту, групі/курсу, викладачу;</li>
 *     <li>експортувати оцінки у CSV-файли.</li>
 * </ul>
 *
 * Меню працює поверх сервісного шару {@link GradeBookService} і
 * {@link ReportService}, не містить бізнес-логіки, лише ввод/вивід.
 *
 * @author Pashchenko Maksym
 * @since 26.11.2025
 */
public class ConsoleMenu {

    /**
     * Сервісний шар для CRUD-операцій та обчислення середніх оцінок.
     */
    private final GradeBookService gradeBookService;

    /**
     * Сервіс формування текстових/CSV-звітів.
     */
    private final ReportService reportService;

    /**
     * Базовий каталог для вивантаження CSV-файлів.
     */
    private static final Path OUTPUT_DIR = Path.of("resources/output");

    /**
     * Створює консольне меню з переданими сервісами.
     *
     * @param gradeBookService сервіс роботи з журналом оцінок
     * @param reportService    сервіс формування звітів
     */
    public ConsoleMenu(GradeBookService gradeBookService,
                       ReportService reportService) {
        this.gradeBookService = gradeBookService;
        this.reportService = reportService;
    }

    /**
     * Головний цикл роботи меню.
     *
     * <p>Виводить варіанти дій, читає вибір користувача
     * та викликає відповідні методи обробки, поки користувач
     * не обере пункт {@code 0 - Вихід}.
     */
    public void run() {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = IoUtil.readIntInRange("Ваш вибір: ", 0, 23);
            System.out.println();

            switch (choice) {
                case 1 -> listStudents();
                case 2 -> addStudent();
                case 3 -> addGrade();
                case 4 -> showStudentReport();
                case 5 -> showGroupCourseReport();
                case 6 -> showTeacherReport();
                case 7 -> exportStudentGrades();
                case 8 -> exportGroupCourseGrades();
                case 9 -> exportTeacherGrades();

                case 10 -> addGroup();
                case 11 -> addCourse();
                case 12 -> addTeacher();

                case 13 -> editStudent();
                case 14 -> deleteStudent();

                case 15 -> editGroup();
                case 16 -> deleteGroup();

                case 17 -> editCourse();
                case 18 -> deleteCourse();

                case 19 -> editTeacher();
                case 20 -> deleteTeacher();

                case 21 -> listGroups();
                case 22 -> listCourses();
                case 23 -> listTeachers();

                case 0 -> {
                    running = false;
                    System.out.println("До побачення!");
                }
                default -> System.out.println("Невірний вибір.");
            }

            System.out.println();
        }
    }

    /**
     * Виводить на екран текстове меню з переліком доступних пунктів.
     */
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
        System.out.println("10 - Додати групу");
        System.out.println("11 - Додати курс");
        System.out.println("12 - Додати викладача");
        System.out.println("13 - Редагувати студента");
        System.out.println("14 - Видалити студента");
        System.out.println("15 - Редагувати групу");
        System.out.println("16 - Видалити групу");
        System.out.println("17 - Редагувати курс");
        System.out.println("18 - Видалити курс");
        System.out.println("19 - Редагувати викладача");
        System.out.println("20 - Видалити викладача");
        System.out.println("21 - Показати всі групи");
        System.out.println("22 - Показати всі курси");
        System.out.println("23 - Показати всіх викладачів");
        System.out.println("0 - Вихід");
    }

    // ============================
    // READ: списки
    // ============================

    /**
     * Виводить на екран усіх студентів із бази.
     *
     * <p>Якщо студентів немає, показує відповідне повідомлення.
     */
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

    /**
     * Виводить список усіх академічних груп.
     */
    private void listGroups() {
        List<Group> groups = gradeBookService.getAllGroups();
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
     * Виводить список усіх курсів.
     */
    private void listCourses() {
        List<Course> courses = gradeBookService.getAllCourses();
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
     * Виводить список усіх викладачів.
     */
    private void listTeachers() {
        List<Teacher> teachers = gradeBookService.getAllTeachers();
        if (teachers.isEmpty()) {
            System.out.println("Викладачів поки немає.");
            return;
        }
        System.out.println("=== Список викладачів ===");
        for (Teacher t : teachers) {
            System.out.println(t);
        }
    }

    // ============================
    // CREATE
    // ============================

    /**
     * Обробляє створення нового студента через консоль.
     *
     * <p>Читає ім'я, прізвище, email, групу та рік вступу, викликає
     * {@link GradeBookService#createStudent(String, String, String, Long, int)}
     * і виводить результат.
     */
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

    /**
     * Обробляє створення нової групи (назва + курс).
     */
    private void addGroup() {
        System.out.println("=== Додати групу ===");
        String name = IoUtil.readNonEmptyLine("Назва групи: ");
        int year = IoUtil.readInt("Курс (1-6): ");
        Group created = gradeBookService.createGroup(name, (short) year);
        System.out.println("Групу створено: " + created);
    }

    /**
     * Обробляє створення нового курсу.
     *
     * <p>Читає назву, семестр, рік, викладача та кількість кредитів
     * і викликає {@link GradeBookService#createCourse(String, Integer, Integer, Long, Integer)}.
     */
    private void addCourse() {
        System.out.println("=== Додати курс ===");
        String name = IoUtil.readNonEmptyLine("Назва курсу: ");
        int semester = IoUtil.readInt("Семестр (1-2): ");
        int year = IoUtil.readInt("Рік викладання (наприклад 2024): ");

        System.out.println("Доступні викладачі:");
        List<Teacher> teachers = gradeBookService.getAllTeachers();
        for (Teacher t : teachers) {
            System.out.printf("  %d: %s %s%n",
                    t.getId(), t.getFirstName(), t.getLastName());
        }
        long teacherId = IoUtil.readLong("ID викладача: ");

        int credits = IoUtil.readInt("Кількість кредитів (ECTS): ");

        Course created = gradeBookService.createCourse(
                name, semester, year, teacherId, credits);
        System.out.println("Курс створено: " + created);
    }

    /**
     * Обробляє створення нового викладача.
     *
     * <p>Читає ім'я, прізвище, кафедру та email і викликає
     * {@link GradeBookService#createTeacher(String, String, String, String)}.
     */
    private void addTeacher() {
        System.out.println("=== Додати викладача ===");
        String firstName = IoUtil.readNonEmptyLine("Ім'я: ");
        String lastName = IoUtil.readNonEmptyLine("Прізвище: ");
        String department = IoUtil.readNonEmptyLine("Кафедра/департамент: ");
        String email = IoUtil.readLine("Email (можна пусто): ").trim();
        if (email.isEmpty()) {
            email = null;
        }
        Teacher created = gradeBookService.createTeacher(
                firstName, lastName, department, email);
        System.out.println("Викладача створено: " + created);
    }

    /**
     * Обробляє додавання оцінки студенту з прив'язкою
     * до курсу та, за бажанням, викладача.
     */
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
            Grade grade = gradeBookService.addGrade(studentId, courseId, teacherId, value, date);
            System.out.println("Оцінку додано: " + grade);
        } catch (IllegalArgumentException e) {
            System.out.println("Помилка: " + e.getMessage());
        }
    }

    // ============================
    // UPDATE / DELETE
    // ============================

    /**
     * Редагує існуючого студента:
     * змінює ПІБ, email, групу та рік вступу.
     */
    private void editStudent() {
        System.out.println("=== Редагувати студента ===");
        long id = IoUtil.readLong("ID студента: ");
        Student student = gradeBookService.getStudentById(id);
        if (student == null) {
            System.out.println("Студента з таким ID не знайдено.");
            return;
        }

        System.out.println("Поточні дані: " + student);

        String firstName = IoUtil.readNonEmptyLine("Нове ім'я: ");
        String lastName = IoUtil.readNonEmptyLine("Нове прізвище: ");
        String email = IoUtil.readLine("Новий email: ").trim();
        if (email.isEmpty()) {
            email = null;
        }
        long groupIdRaw = IoUtil.readLong("ID групи (0 якщо без групи): ");
        Long groupId = groupIdRaw == 0 ? null : groupIdRaw;
        int enrollmentYear = IoUtil.readInt("Рік вступу: ");

        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setGroupId(groupId);
        student.setEnrollmentYear((short) enrollmentYear);

        boolean ok = gradeBookService.updateStudent(student);
        System.out.println(ok ? "Студента оновлено." : "Помилка оновлення студента.");
    }

    /**
     * Видаляє студента за введеним ID.
     */
    private void deleteStudent() {
        System.out.println("=== Видалити студента ===");
        long id = IoUtil.readLong("ID студента: ");
        boolean ok = gradeBookService.deleteStudent(id);
        System.out.println(ok ? "Студента видалено." : "Студента з таким ID не знайдено.");
    }

    /**
     * Редагує дані групи (назва та курс).
     */
    private void editGroup() {
        System.out.println("=== Редагувати групу ===");
        long id = IoUtil.readLong("ID групи: ");
        Group group = gradeBookService.getGroupById(id);
        if (group == null) {
            System.out.println("Групу з таким ID не знайдено.");
            return;
        }
        System.out.println("Поточні дані: " + group);

        String name = IoUtil.readNonEmptyLine("Нова назва групи: ");
        int year = IoUtil.readInt("Новий курс (1-6): ");

        group.setName(name);
        group.setYear((short) year);

        boolean ok = gradeBookService.updateGroup(group);
        System.out.println(ok ? "Групу оновлено." : "Помилка оновлення групи.");
    }

    /**
     * Видаляє групу за введеним ID.
     */
    private void deleteGroup() {
        System.out.println("=== Видалити групу ===");
        long id = IoUtil.readLong("ID групи: ");
        boolean ok = gradeBookService.deleteGroup(id);
        System.out.println(ok ? "Групу видалено." : "Групу з таким ID не знайдено.");
    }

    /**
     * Редагує дані курсу (назву, семестр, рік, викладача, кредити).
     */
    private void editCourse() {
        System.out.println("=== Редагувати курс ===");
        long id = IoUtil.readLong("ID курсу: ");
        Course course = gradeBookService.getCourseById(id);
        if (course == null) {
            System.out.println("Курс з таким ID не знайдено.");
            return;
        }
        System.out.println("Поточні дані: " + course);

        String name = IoUtil.readNonEmptyLine("Нова назва курсу: ");
        int semester = IoUtil.readInt("Семестр (1-2): ");
        int year = IoUtil.readInt("Рік викладання: ");
        long teacherId = IoUtil.readLong("ID викладача: ");
        int credits = IoUtil.readInt("Кількість кредитів (ECTS): ");

        course.setName(name);
        course.setSemester((short) semester);
        course.setYear((short) year);
        course.setTeacherId(teacherId);
        course.setCredits((short) credits);

        boolean ok = gradeBookService.updateCourse(course);
        System.out.println(ok ? "Курс оновлено." : "Помилка оновлення курсу.");
    }

    /**
     * Видаляє курс за введеним ID.
     */
    private void deleteCourse() {
        System.out.println("=== Видалити курс ===");
        long id = IoUtil.readLong("ID курсу: ");
        boolean ok = gradeBookService.deleteCourse(id);
        System.out.println(ok ? "Курс видалено." : "Курс з таким ID не знайдено.");
    }

    /**
     * Редагує дані викладача (ПІБ, департамент, email).
     */
    private void editTeacher() {
        System.out.println("=== Редагувати викладача ===");
        long id = IoUtil.readLong("ID викладача: ");
        Teacher teacher = gradeBookService.getTeacherById(id);
        if (teacher == null) {
            System.out.println("Викладача з таким ID не знайдено.");
            return;
        }
        System.out.println("Поточні дані: " + teacher);

        String firstName = IoUtil.readNonEmptyLine("Нове ім'я: ");
        String lastName = IoUtil.readNonEmptyLine("Нове прізвище: ");
        String department = IoUtil.readNonEmptyLine("Новий департамент: ");
        String email = IoUtil.readLine("Новий email (можна пусто): ").trim();
        if (email.isEmpty()) {
            email = null;
        }

        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setDepartment(department);
        teacher.setEmail(email);

        boolean ok = gradeBookService.updateTeacher(teacher);
        System.out.println(ok ? "Викладача оновлено." : "Помилка оновлення викладача.");
    }

    /**
     * Видаляє викладача за введеним ID.
     */
    private void deleteTeacher() {
        System.out.println("=== Видалити викладача ===");
        long id = IoUtil.readLong("ID викладача: ");
        boolean ok = gradeBookService.deleteTeacher(id);
        System.out.println(ok ? "Викладача видалено." : "Викладача з таким ID не знайдено.");
    }

    // ============================
    // Звіти
    // ============================

    /**
     * Виводить на екран детальний звіт по студенту:
     * його оцінки та середній бал (через {@link ReportService}).
     */
    private void showStudentReport() {
        System.out.println("=== Звіт по студенту ===");
        long studentId = IoUtil.readLong("ID студента: ");
        reportService.printStudentReport(studentId);
    }

    /**
     * Виводить звіт по конкретній групі та курсу:
     * усі оцінки студентів цієї групи і середній бал.
     */
    private void showGroupCourseReport() {
        System.out.println("=== Звіт по групі та курсу ===");
        long groupId = IoUtil.readLong("ID групи: ");
        long courseId = IoUtil.readLong("ID курсу: ");
        reportService.printGroupCourseReport(groupId, courseId);
    }

    /**
     * Виводить звіт по викладачу:
     * усі виставлені ним оцінки та середній бал.
     */
    private void showTeacherReport() {
        System.out.println("=== Звіт по викладачу ===");
        long teacherId = IoUtil.readLong("ID викладача: ");
        reportService.printTeacherReport(teacherId);
    }

    // ============================
    // CSV export helpers
    // ============================

    /**
     * Експортує всі оцінки студента у CSV-файл в {@link #OUTPUT_DIR}.
     *
     * <p>Ім'я файлу має вигляд:
     * {@code student_<id>_grades.csv}.
     */
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

    /**
     * Експортує всі оцінки групи по курсу у CSV-файл.
     *
     * <p>Ім'я файлу має вигляд:
     * {@code group_<groupId>_course_<courseId>_grades.csv}.
     */
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

    /**
     * Експортує всі оцінки, виставлені певним викладачем, у CSV-файл.
     *
     * <p>Ім'я файлу має вигляд:
     * {@code teacher_<teacherId>_grades.csv}.
     */
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
