package main.java.ua.knu.pashchenko_maksym.util;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import main.java.ua.knu.pashchenko_maksym.model.Grade;
import main.java.ua.knu.pashchenko_maksym.model.Student;


public final class CsvUtil {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE;

    private CsvUtil() {
    }


    public static void writeStudentsToCsv(List<Student> students, Path file)
            throws IOException {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {

            writer.write("id;first_name;last_name;email;group_id;enrollment_year;created_at");
            writer.newLine();

            for (Student s : students) {
                String id = s.getId() != null ? s.getId().toString() : "";
                String firstName = safe(s.getFirstName());
                String lastName = safe(s.getLastName());
                String email = safe(s.getEmail());
                String groupId = s.getGroupId() != null ? s.getGroupId().toString() : "";
                String year = s.getEnrollmentYear() != null ? s.getEnrollmentYear().toString() : "";
                String created = s.getCreatedAt() != null
                        ? DATE_TIME_FORMATTER.format(s.getCreatedAt())
                        : "";

                writer.write(String.join(";", id, firstName, lastName, email,
                        groupId, year, created));
                writer.newLine();
            }
        }
    }

    public static void writeGradesToCsv(List<Grade> grades, Path file)
            throws IOException {

        try (BufferedWriter writer =
                     Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {

            writer.write("id;student_id;course_id;teacher_id;value;grade_date");
            writer.newLine();

            for (Grade g : grades) {
                String id = g.getId() != null ? g.getId().toString() : "";
                String studentId = g.getStudentId() != null ? g.getStudentId().toString() : "";
                String courseId = g.getCourseId() != null ? g.getCourseId().toString() : "";
                String teacherId = g.getTeacherId() != null ? g.getTeacherId().toString() : "";
                String value = g.getValue() != null ? g.getValue().toPlainString() : "";
                String date = g.getGradeDate() != null
                        ? DATE_FORMATTER.format(g.getGradeDate())
                        : "";

                writer.write(String.join(";", id, studentId, courseId, teacherId, value, date));
                writer.newLine();
            }
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}

