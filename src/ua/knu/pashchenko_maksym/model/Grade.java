package ua.knu.pashchenko_maksym.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Grade {

    private Long id;
    private Long studentId;
    private Long courseId;
    private Long teacherId;
    private BigDecimal value;
    private LocalDate gradeDate;

    public Grade() {
    }

    public Grade(Long id,
                 Long studentId,
                 Long courseId,
                 Long teacherId,
                 BigDecimal value,
                 LocalDate gradeDate) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.teacherId = teacherId;
        this.value = value;
        this.gradeDate = gradeDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDate getGradeDate() {
        return gradeDate;
    }

    public void setGradeDate(LocalDate gradeDate) {
        this.gradeDate = gradeDate;
    }

    @Override
    public String toString() {
        return "Grade{" + id
                + ", student=" + studentId
                + ", course=" + courseId
                + ", value=" + value
                + ", date=" + gradeDate + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Grade)) {
            return false;
        }
        Grade grade = (Grade) o;
        return Objects.equals(id, grade.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

