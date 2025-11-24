package ua.knu.pashchenko_maksym.dao;

import java.util.List;
import ua.knu.pashchenko_maksym.model.Teacher;

public interface TeacherDao {

    Teacher findById(Long id);

    List<Teacher> findAll();

    List<Teacher> findByLastName(String lastName);

    Teacher insert(Teacher teacher);

    boolean update(Teacher teacher);

    boolean delete(Long id);
}
