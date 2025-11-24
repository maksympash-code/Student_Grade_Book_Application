package main.java.ua.knu.pashchenko_maksym.dao;

import java.util.List;
import main.java.ua.knu.pashchenko_maksym.model.Grade;

public interface GradeDao {

    Grade findById(Long id);

    List<Grade> findAll();

    Grade insert(Grade grade);

    boolean update(Grade grade);

    boolean delete(Long id);

    List<Grade> findByStudentId(Long studentId);

    List<Grade> findByCourseId(Long courseId);

    List<Grade> findByTeacherId(Long teacherId);

    List<Grade> findByStudentAndCourse(Long studentId, Long courseId);
}

