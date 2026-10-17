package hrms.util;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Centralized JDBC connection to a local embedded Apache Derby database. */
public final class DBConnection {
    private static final String DATABASE_PATH = setting("hrms.db.path", "HRMS_DB_PATH", "hrmsdb");
    private static final String URL = "jdbc:derby:" + normalizePath(DATABASE_PATH) + ";create=true";

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static String getDatabaseUrl() { return URL; }

    private static String normalizePath(String path) {
        return Path.of(path).normalize().toString().replace('\\', '/');
    }

    private static String setting(String property, String environment, String defaultValue) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) value = System.getenv(environment);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
