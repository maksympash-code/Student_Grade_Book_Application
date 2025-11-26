package ua.knu.pashchenko_maksym.dao;

import ua.knu.pashchenko_maksym.dao.exception.DaoException;
import ua.knu.pashchenko_maksym.model.Group;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-реалізація {@link GroupDao} для таблиці {@code groups}.
 *
 * <p>Інкапсулює всі типові операції над академічними групами:
 * пошук, вставку, оновлення та видалення. Працює без ORM,
 * напряму через {@link java.sql.Connection} та {@link PreparedStatement}.
 *
 * @author Pashchenko Maksym
 * @since 26.11.2025
 */
public class JdbcGroupDao implements GroupDao {

    private static final String FIND_BY_ID_SQL =
            "SELECT id, name, year FROM groups WHERE id = ?";

    private static final String FIND_ALL_SQL =
            "SELECT id, name, year FROM groups ORDER BY name";

    private static final String INSERT_SQL =
            "INSERT INTO groups(name, year) VALUES (?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE groups SET name = ?, year = ? WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM groups WHERE id = ?";

    private static final String FIND_BY_NAME_SQL =
            "SELECT id, name, year FROM groups WHERE name = ?";

    // =============================
    // findById (Long) + findById(long)
    // =============================

    /**
     * Повертає групу за її id.
     *
     * @param id ідентифікатор групи, не {@code null}
     * @return знайдена {@link Group} або {@code null}, якщо групу не знайдено
     * @throws IllegalArgumentException якщо {@code id == null}
     * @throws DaoException             у разі помилки доступу до БД
     */
    @Override
    public Group findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return findByIdInternal(id);
    }

    /**
     * Повертає групу як {@link Optional}, зручну для подальшої обробки.
     *
     * @param id ідентифікатор групи
     * @return {@link Optional} з {@link Group} або {@link Optional#empty()}
     * @throws DaoException у разі помилки доступу до БД
     */
    @Override
    public Optional<Group> findById(long id) {
        return Optional.ofNullable(findByIdInternal(id));
    }

    /**
     * Внутрішня реалізація пошуку групи за id.
     *
     * @param id ідентифікатор групи
     * @return група або {@code null}, якщо не знайдено
     * @throws DaoException у разі помилки доступу до БД
     */
    private Group findByIdInternal(long id) {
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_ID_SQL)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DaoException("Error finding group by id = " + id, e);
        }
    }

    // =============================
    // findAll
    // =============================

    /**
     * Повертає всі групи, відсортовані за назвою.
     *
     * @return список груп (може бути порожнім, але не {@code null})
     * @throws DaoException у разі помилки доступу до БД
     */
    @Override
    public List<Group> findAll() {
        List<Group> result = new ArrayList<>();

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }

            return result;
        } catch (SQLException e) {
            throw new DaoException("Error loading all groups", e);
        }
    }

    // =============================
    // findByName
    // =============================

    /**
     * Пошук групи за унікальною назвою.
     *
     * @param name назва групи (наприклад, {@code "IP-11"}), не {@code null}
     * @return знайдена група або {@code null}, якщо такої немає
     * @throws IllegalArgumentException якщо {@code name == null}
     * @throws DaoException             у разі помилки доступу до БД
     */
    @Override
    public Group findByName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(FIND_BY_NAME_SQL)) {

            ps.setString(1, name);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

            return null;
        } catch (SQLException e) {
            throw new DaoException("Error finding group by name = " + name, e);
        }
    }

    // =============================
    // insert
    // =============================

    /**
     * Додає нову групу в таблицю {@code groups}.
     *
     * <p>Після успішної вставки у переданий об'єкт записується
     * згенерований первинний ключ {@link Group#getId()}.
     *
     * @param group група для вставки, не {@code null}
     * @return та сама група з оновленим id
     * @throws IllegalArgumentException якщо {@code group == null}
     * @throws DaoException             у разі помилки доступу до БД
     */
    @Override
    public Group insert(Group group) {
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     INSERT_SQL,
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, group.getName());
            if (group.getYear() != null) {
                ps.setShort(2, group.getYear());
            } else {
                ps.setNull(2, java.sql.Types.SMALLINT);
            }

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DaoException("Inserting group failed, no rows affected: " + group);
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    group.setId(id);
                } else {
                    throw new DaoException("Inserting group failed, no ID obtained: " + group);
                }
            }

            return group;
        } catch (SQLException e) {
            throw new DaoException("Error inserting group " + group, e);
        }
    }

    // =============================
    // update
    // =============================

    /**
     * Оновлює існуючу групу.
     *
     * @param group група з заповненим {@link Group#getId()} та новими даними
     * @return {@code true}, якщо хоча б один рядок оновлено,
     *         {@code false}, якщо групу з таким id не знайдено
     * @throws IllegalArgumentException якщо {@code group == null} або {@code group.getId() == null}
     * @throws DaoException             у разі помилки доступу до БД
     */
    @Override
    public boolean update(Group group) {
        if (group == null || group.getId() == null) {
            throw new IllegalArgumentException("group and group.id must not be null");
        }

        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, group.getName());
            if (group.getYear() != null) {
                ps.setShort(2, group.getYear());
            } else {
                ps.setNull(2, java.sql.Types.SMALLINT);
            }
            ps.setLong(3, group.getId());

            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new DaoException("Error updating group " + group, e);
        }
    }

    // =============================
    // delete(Long) + delete(long)
    // =============================

    /**
     * Видаляє групу за id (обгортка над {@link #delete(long)}).
     *
     * @param id ідентифікатор групи, не {@code null}
     * @return {@code true}, якщо групу було видалено,
     *         {@code false}, якщо запис не знайдено
     * @throws IllegalArgumentException якщо {@code id == null}
     * @throws DaoException             у разі помилки доступу до БД
     */
    @Override
    public boolean delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return delete(id.longValue());
    }

    /**
     * Видаляє групу за id.
     *
     * @param id ідентифікатор групи
     * @return {@code true}, якщо групу було видалено,
     *         {@code false}, якщо запис не знайдено
     * @throws DaoException у разі помилки доступу до БД
     */
    @Override
    public boolean delete(long id) {
        try (Connection connection = DataSourceProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE_SQL)) {

            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new DaoException("Error deleting group with id = " + id, e);
        }
    }

    // =============================
    // mapper
    // =============================

    /**
     * Мапінг поточного рядка {@link ResultSet} до об'єкта {@link Group}.
     *
     * @param rs result set, позиціонований на рядку з даними групи
     * @return заповнений об'єкт {@link Group}
     * @throws SQLException у разі помилки читання з {@link ResultSet}
     */
    private Group mapRow(ResultSet rs) throws SQLException {
        Group group = new Group();
        group.setId(rs.getLong("id"));
        group.setName(rs.getString("name"));
        group.setYear(rs.getShort("year"));
        return group;
    }
}
