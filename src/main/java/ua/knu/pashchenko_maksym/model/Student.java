package main.java.ua.knu.pashchenko_maksym.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Student {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Long groupId;
    private Short enrollmentYear;
    private LocalDateTime createdAt;

    public Student() {
    }

    public Student(Long id,
                   String firstName,
                   String lastName,
                   String email,
                   Long groupId,
                   Short enrollmentYear,
                   LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.groupId = groupId;
        this.enrollmentYear = enrollmentYear;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Short getEnrollmentYear() {
        return enrollmentYear;
    }

    public void setEnrollmentYear(Short enrollmentYear) {
        this.enrollmentYear = enrollmentYear;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Student{" + id + ", " + firstName + " " + lastName + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Student)) {
            return false;
        }
        Student student = (Student) o;
        return Objects.equals(id, student.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


