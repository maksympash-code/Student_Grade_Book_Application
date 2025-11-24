package main.java.ua.knu.pashchenko_maksym.dao;

import java.util.List;
import main.java.ua.knu.pashchenko_maksym.model.Course;

public interface CourseDao {

    Course findById(Long id);

    List<Course> findAll();

    Course findByName(String name);

    List<Course> findByTeacherId(Long teacherId);

    Course insert(Course course);

    boolean update(Course course);

    boolean delete(Long id);
}

