package main.java.ua.knu.pashchenko_maksym.model;

import java.util.Objects;

public class Course {

    private Long id;
    private String name;
    private Short semester;
    private Short year;
    private Long teacherId;
    private Short credits;

    public Course() {
    }

    public Course(Long id,
                  String name,
                  Short semester,
                  Short year,
                  Long teacherId,
                  Short credits) {
        this.id = id;
        this.name = name;
        this.semester = semester;
        this.year = year;
        this.teacherId = teacherId;
        this.credits = credits;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Short getSemester() {
        return semester;
    }

    public void setSemester(Short semester) {
        this.semester = semester;
    }

    public Short getYear() {
        return year;
    }

    public void setYear(Short year) {
        this.year = year;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Short getCredits() {
        return credits;
    }

    public void setCredits(Short credits) {
        this.credits = credits;
    }

    @Override
    public String toString() {
        return "Course{" + id + ", '" + name + "', sem=" + semester + ", year=" + year + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Course)) {
            return false;
        }
        Course course = (Course) o;
        return Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

