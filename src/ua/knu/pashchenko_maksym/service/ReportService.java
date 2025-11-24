package ua.knu.pashchenko_maksym.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import ua.knu.pashchenko_maksym.dao.CourseDao;
import ua.knu.pashchenko_maksym.dao.GradeDao;
import ua.knu.pashchenko_maksym.dao.GroupDao;
import ua.knu.pashchenko_maksym.dao.StudentDao;
import ua.knu.pashchenko_maksym.dao.TeacherDao;
import ua.knu.pashchenko_maksym.model.Course;
import ua.knu.pashchenko_maksym.model.Grade;
import ua.knu.pashchenko_maksym.model.Group;
import ua.knu.pashchenko_maksym.model.Student;
import ua.knu.pashchenko_maksym.model.Teacher;
import ua.knu.pashchenko_maksym.util.CsvUtil;

/**
 * Generates reports and exports them to console and CSV files.
 */
public class ReportService {

    private final StudentDao studentDao;
    private final GroupDao groupDao;
    private final CourseDao courseDao;
    private final TeacherDao teacherDao;
    private final GradeDao gradeDao;
    private final GradeBookService gradeBookService;

    public ReportService(StudentDao studentDao,
                         GroupDao groupDao,
                         CourseDao courseDao,
                         TeacherDao teacherDao,
                         GradeDao gradeDao,
                         GradeBookService gradeBookService) {
        this.studentDao = studentDao;
        this.groupDao = groupDao;
        this.courseDao = courseDao;
        this.teacherDao = teacherDao;
        this.gradeDao = gradeDao;
        this.gradeBookService = gradeBookService;
    }

    // =========================
    // STUDENT REPORT
    // =========================

    /**
     * Prints to console full report for a student: all courses and average grade.
     */
    public void printStudentReport(Long studentId) {
        Student student = studentDao.findById(studentId);
        if (student == null) {
            System.out.println("Student with id " + studentId + " not found.");
            return;
        }

        List<Grade> grades = gradeDao.findByStudentId(studentId);
        double avg = gradeBookService.getStudentAverageGrade(studentId);

        System.out.println("=== Student report ===");
        System.out.printf("Student: %s %s (id=%d)%n",
                student.getFirstName(), student.getLastName(), student.getId());
        System.out.println("Email: " + student.getEmail());
        System.out.println("Grades:");

        if (grades.isEmpty()) {
            System.out.println("  No grades yet.");
        } else {
            for (Grade g : grades) {
                Course course = courseDao.findById(g.getCourseId());
                String courseName = course != null ? course.getName() : ("courseId=" + g.getCourseId());
                System.out.printf("  - %s: %.2f (%s)%n",
                        courseName,
                        g.getValue() != null ? g.getValue().doubleValue() : 0.0,
                        g.getGradeDate());
            }
        }

        System.out.printf("Average grade: %.2f%n", avg);
    }

    /**
     * Exports all grades of a student to CSV using CsvUtil.
     */
    public void exportStudentGradesToCsv(Long studentId, Path file) throws IOException {
        List<Grade> grades = gradeDao.findByStudentId(studentId);
        CsvUtil.writeGradesToCsv(grades, file);
    }

    // =========================
    // GROUP + COURSE REPORT
    // =========================

    /**
     * Prints report for a group in a concrete course:
     * each student's grades and group average.
     */
    public void printGroupCourseReport(Long groupId, Long courseId) {
        Group group = groupDao.findById(groupId);
        Course course = courseDao.findById(courseId);

        if (group == null) {
            System.out.println("Group with id " + groupId + " not found.");
            return;
        }
        if (course == null) {
            System.out.println("Course with id " + courseId + " not found.");
            return;
        }

        List<Student> students = studentDao.findByGroupId(groupId);
        System.out.println("=== Group-course report ===");
        System.out.printf("Group: %s (id=%d)%n", group.getName(), group.getId());
        System.out.printf("Course: %s (id=%d)%n", course.getName(), course.getId());

        if (students.isEmpty()) {
            System.out.println("No students in this group.");
            return;
        }

        double groupSum = 0.0;
        int gradeCount = 0;

        for (Student s : students) {
            List<Grade> grades = gradeDao.findByStudentAndCourse(s.getId(), courseId);
            if (grades.isEmpty()) {
                System.out.printf("  %s %s: no grades%n", s.getFirstName(), s.getLastName());
            } else {
                System.out.printf("  %s %s:%n", s.getFirstName(), s.getLastName());
                for (Grade g : grades) {
                    double v = g.getValue() != null ? g.getValue().doubleValue() : 0.0;
                    System.out.printf("    - %.2f (%s)%n", v, g.getGradeDate());
                    groupSum += v;
                    gradeCount++;
                }
            }
        }

        double groupAverage = gradeCount == 0 ? 0.0 : groupSum / gradeCount;
        System.out.printf("Group average for course '%s': %.2f%n",
                course.getName(), groupAverage);
    }

    /**
     * Exports all grades for given group and course to CSV.
     * Uses CsvUtil to actually write the file.
     */
    public void exportGroupCourseGradesToCsv(Long groupId, Long courseId, Path file)
            throws IOException {

        List<Student> students = studentDao.findByGroupId(groupId);
        List<Grade> allGrades = new ArrayList<>();

        for (Student s : students) {
            allGrades.addAll(gradeDao.findByStudentAndCourse(s.getId(), courseId));
        }

        CsvUtil.writeGradesToCsv(allGrades, file);
    }

    // =========================
    // TEACHER REPORT
    // =========================

    /**
     * Prints report for a teacher: all grades they issued and average.
     */
    public void printTeacherReport(Long teacherId) {
        Teacher teacher = teacherDao.findById(teacherId);
        if (teacher == null) {
            System.out.println("Teacher with id " + teacherId + " not found.");
            return;
        }

        List<Grade> grades = gradeDao.findByTeacherId(teacherId);
        double avg = gradeBookService.getTeacherAverageGrade(teacherId);

        System.out.println("=== Teacher report ===");
        System.out.printf("Teacher: %s %s (id=%d)%n",
                teacher.getFirstName(), teacher.getLastName(), teacher.getId());
        System.out.println("Department: " + teacher.getDepartment());
        System.out.println("Email: " + teacher.getEmail());

        if (grades.isEmpty()) {
            System.out.println("No grades issued by this teacher.");
        } else {
            System.out.println("Grades:");
            for (Grade g : grades) {
                Course course = courseDao.findById(g.getCourseId());
                Student student = studentDao.findById(g.getStudentId());
                String courseName = course != null ? course.getName() : ("courseId=" + g.getCourseId());
                String studentName = student != null
                        ? student.getFirstName() + " " + student.getLastName()
                        : ("studentId=" + g.getStudentId());

                System.out.printf("  %s -> %s: %.2f (%s)%n",
                        courseName,
                        studentName,
                        g.getValue() != null ? g.getValue().doubleValue() : 0.0,
                        g.getGradeDate());
            }
        }

        System.out.printf("Average grade for teacher: %.2f%n", avg);
    }

    /**
     * Exports all grades issued by a teacher to CSV.
     */
    public void exportTeacherGradesToCsv(Long teacherId, Path file) throws IOException {
        List<Grade> grades = gradeDao.findByTeacherId(teacherId);
        CsvUtil.writeGradesToCsv(grades, file);
    }

    /**
     * Writes arbitrary text report to a file (UTF-8).
     */
    public void writeTextReportToFile(String content, Path file) throws IOException {
        try (BufferedWriter writer =
                     Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
}
