package hrms.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

/** Creates the HRMS schema and safe demonstration/reference rows in embedded Derby. */
public final class DatabaseSetup {
    private DatabaseSetup() { }

    public static void initialize() throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            createIfMissing(connection, "USERS",
                "CREATE TABLE users ("
                + "user_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "username VARCHAR(30) NOT NULL, password_hash VARCHAR(255) NOT NULL,"
                + "full_name VARCHAR(80) NOT NULL, role VARCHAR(20) NOT NULL,"
                + "CONSTRAINT pk_users PRIMARY KEY (user_id), CONSTRAINT uq_users_username UNIQUE (username),"
                + "CONSTRAINT ck_users_role CHECK (role IN ('ADMIN','HR_STAFF')))" );
            createIfMissing(connection, "DEPARTMENTS",
                "CREATE TABLE departments ("
                + "department_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "department_name VARCHAR(60) NOT NULL, description VARCHAR(150),"
                + "CONSTRAINT pk_departments PRIMARY KEY (department_id), CONSTRAINT uq_departments_name UNIQUE (department_name))");
            createIfMissing(connection, "POSITIONS",
                "CREATE TABLE positions ("
                + "position_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "position_title VARCHAR(60) NOT NULL, description VARCHAR(150),"
                + "CONSTRAINT pk_positions PRIMARY KEY (position_id), CONSTRAINT uq_positions_title UNIQUE (position_title))");
            createIfMissing(connection, "EMPLOYEES",
                "CREATE TABLE employees ("
                + "employee_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "employee_no VARCHAR(15) NOT NULL, first_name VARCHAR(40) NOT NULL, last_name VARCHAR(40) NOT NULL,"
                + "birth_date DATE NOT NULL, gender VARCHAR(10) NOT NULL, email VARCHAR(80), phone VARCHAR(20), address VARCHAR(150),"
                + "date_hired DATE NOT NULL, status VARCHAR(15) NOT NULL, department_id INTEGER NOT NULL, position_id INTEGER NOT NULL,"
                + "CONSTRAINT pk_employees PRIMARY KEY (employee_id), CONSTRAINT uq_employees_no UNIQUE (employee_no),"
                + "CONSTRAINT ck_employees_status CHECK (status IN ('ACTIVE','ON LEAVE','RESIGNED'))," 
                + "CONSTRAINT fk_employees_department FOREIGN KEY (department_id) REFERENCES departments(department_id),"
                + "CONSTRAINT fk_employees_position FOREIGN KEY (position_id) REFERENCES positions(position_id))");
            createIfMissing(connection, "ATTENDANCE",
                "CREATE TABLE attendance ("
                + "attendance_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "employee_id INTEGER NOT NULL, attendance_date DATE NOT NULL, time_in TIME, time_out TIME, status VARCHAR(15) NOT NULL,"
                + "CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),"
                + "CONSTRAINT uq_attendance_employee_date UNIQUE (employee_id, attendance_date),"
                + "CONSTRAINT ck_attendance_status CHECK (status IN ('PRESENT','LATE','ABSENT','ON LEAVE'))," 
                + "CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id))");
            // Older builds rejected overnight shifts through CK_ATTENDANCE_TIME.
            // Remove that obsolete constraint safely from an existing local Derby database.
            removeLegacyAttendanceTimeConstraint(connection);
            createIfMissing(connection, "LEAVE_REQUESTS",
                "CREATE TABLE leave_requests ("
                + "leave_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                + "employee_id INTEGER NOT NULL, leave_type VARCHAR(30) NOT NULL, start_date DATE NOT NULL, end_date DATE NOT NULL,"
                + "reason VARCHAR(200), status VARCHAR(15) NOT NULL DEFAULT 'PENDING', date_filed DATE NOT NULL,"
                + "CONSTRAINT pk_leave_requests PRIMARY KEY (leave_id),"
                + "CONSTRAINT ck_leave_status CHECK (status IN ('PENDING','APPROVED','REJECTED'))," 
                + "CONSTRAINT ck_leave_dates CHECK (end_date >= start_date),"
                + "CONSTRAINT fk_leave_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id))");
        }
        seedUser("admin", "admin123", "System Administrator", "ADMIN");
        seedUser("hrstaff", "hr123", "HR Staff Demo", "HR_STAFF");
        seedDepartment("Human Resources", "Human resources and employee services");
        seedDepartment("Administration", "General administrative operations");
        seedPosition("HR Officer", "Handles employee records and HR administration");
        seedPosition("Department Manager", "Manages department activities");
    }

    private static void removeLegacyAttendanceTimeConstraint(Connection connection) throws SQLException {
        String query = "SELECT COUNT(*) FROM SYS.SYSCONSTRAINTS c "
            + "JOIN SYS.SYSTABLES t ON c.TABLEID = t.TABLEID "
            + "WHERE c.CONSTRAINTNAME = 'CK_ATTENDANCE_TIME' AND t.TABLENAME = 'ATTENDANCE'";
        boolean exists;
        try (Statement statement = connection.createStatement(); ResultSet result = statement.executeQuery(query)) {
            result.next();
            exists = result.getInt(1) > 0;
        }
        if (exists) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("ALTER TABLE attendance DROP CONSTRAINT ck_attendance_time");
            }
        }
    }

    private static void createIfMissing(Connection connection, String tableName, String sql) throws SQLException {
        if (tableExists(connection, tableName)) return;
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData meta = connection.getMetaData();
        try (ResultSet result = meta.getTables(null, "APP", tableName.toUpperCase(Locale.ROOT), new String[] {"TABLE"})) {
            return result.next();
        }
    }

    private static void seedUser(String username, String password, String fullName, String role) throws SQLException {
        if (exists("users", "username", username)) return;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)")) {
            insert.setString(1, username);
            insert.setString(2, PasswordUtil.hash(password));
            insert.setString(3, fullName);
            insert.setString(4, role);
            insert.executeUpdate();
        }
    }

    private static void seedDepartment(String name, String description) throws SQLException {
        if (exists("departments", "department_name", name)) return;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO departments (department_name, description) VALUES (?, ?)")) {
            insert.setString(1, name); insert.setString(2, description); insert.executeUpdate();
        }
    }

    private static void seedPosition(String title, String description) throws SQLException {
        if (exists("positions", "position_title", title)) return;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement insert = connection.prepareStatement(
                 "INSERT INTO positions (position_title, description) VALUES (?, ?)")) {
            insert.setString(1, title); insert.setString(2, description); insert.executeUpdate();
        }
    }

    private static boolean exists(String table, String column, String value) throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE " + column + " = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet result = statement.executeQuery()) { return result.next(); }
        }
    }
}
