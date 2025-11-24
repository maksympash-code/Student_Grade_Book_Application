package main.java.ua.knu.pashchenko_maksym.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DataSourceProvider {

    private static final String URL = "jdbc:postgresql://localhost:5432/gradebook";
    private static final String USER = "postgres";      // TODO: заміни на свій логін
    private static final String PASSWORD = "password";  // TODO: заміни на свій пароль

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("PostgreSQL JDBC driver not found");
        }
    }

    private DataSourceProvider() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

