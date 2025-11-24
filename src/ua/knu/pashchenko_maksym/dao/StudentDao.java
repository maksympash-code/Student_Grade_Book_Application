package ua.knu.pashchenko_maksym.dao;

import java.util.List;
import ua.knu.pashchenko_maksym.model.Student;

public interface StudentDao {

    Student findById(Long id);

    List<Student> findAll();

    Student insert(Student student);

    boolean update(Student student);

    boolean delete(Long id);

    List<Student> findByGroupId(Long groupId);

    List<Student> findByCourseId(Long courseId);
}

