package ua.knu.pashchenko_maksym.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides JDBC {@link Connection} instances to the PostgreSQL database used by the
 * Student Grade Book application.
 *
 * <p>Database parameters (URL, user, password) зараз захардкожені в константах, але за
 * потреби можуть бути винесені в конфігураційний файл або змінні оточення.
 *
 * @author Pashchenko Maksym
 * @since 26.11.2025
 */
public final class DataSourceProvider {

    private static final String URL = "jdbc:postgresql://localhost:5432/gradebook";
    private static final String USER = "postgres";
    private static final String PASSWORD = "12345";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("PostgreSQL JDBC driver not found");
        }
    }

    private DataSourceProvider() {
    }

    /**
     * Returns a new JDBC {@link Connection} to the configured PostgreSQL database.
     *
     * @return fresh open {@link Connection} instance
     * @throws SQLException if a database access error occurs or the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
