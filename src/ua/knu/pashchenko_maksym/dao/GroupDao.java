package ua.knu.pashchenko_maksym.dao;

import java.util.List;
import ua.knu.pashchenko_maksym.model.Group;

public interface GroupDao {

    Group findById(Long id);

    List<Group> findAll();

    Group findByName(String name);

    Group insert(Group group);

    boolean update(Group group);

    boolean delete(Long id);
}

