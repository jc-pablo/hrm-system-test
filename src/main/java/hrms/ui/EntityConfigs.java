package hrms.ui;

import java.time.LocalDate;
import java.util.List;
import static hrms.ui.EntityConfig.FieldDef.*;

/** Database-backed screen configurations using ANSI SQL supported by Apache Derby. */
public final class EntityConfigs {
    private EntityConfigs() { }

    public static EntityConfig users() {
        return new EntityConfig("User Accounts", "users", "user_id",
            "SELECT user_id AS ID, username AS Username, full_name AS \"Full Name\", role AS Role FROM users",
            List.of(text("username", "Username", true), password("password_hash", "Password", true),
                    text("full_name", "Full Name", true), combo("role", "Role", true, "ADMIN", "HR_STAFF")),
            List.of("username", "full_name", "role"));
    }

    public static EntityConfig departments() {
        return new EntityConfig("Departments", "departments", "department_id",
            "SELECT department_id AS ID, department_name AS \"Department Name\", description AS Description FROM departments",
            List.of(text("department_name", "Department Name", true), text("description", "Description", false)),
            List.of("department_name", "description"));
    }

    public static EntityConfig positions() {
        return new EntityConfig("Positions", "positions", "position_id",
            "SELECT position_id AS ID, position_title AS \"Position Title\", description AS Description FROM positions",
            List.of(text("position_title", "Position Title", true), text("description", "Description", false)),
            List.of("position_title", "description"));
    }

    public static EntityConfig employees() {
        return new EntityConfig("Employees", "employees", "employee_id",
            "SELECT e.employee_id AS ID, e.employee_no AS \"Employee No.\", e.first_name AS \"First Name\", "
            + "e.last_name AS \"Last Name\", e.birth_date AS \"Birth Date\", e.gender AS Gender, "
            + "e.email AS Email, e.phone AS Phone, e.date_hired AS \"Date Hired\", e.status AS Status, "
            + "d.department_name AS Department, p.position_title AS Position "
            + "FROM employees e JOIN departments d ON e.department_id = d.department_id "
            + "JOIN positions p ON e.position_id = p.position_id",
            List.of(text("employee_no", "Employee No.", true), text("first_name", "First Name", true),
                text("last_name", "Last Name", true), date("birth_date", "Birth Date (YYYY-MM-DD)", true, ""),
                combo("gender", "Gender", true, "Female", "Male", "Other"), text("email", "Email", false),
                text("phone", "Phone", false), text("address", "Address", false),
                date("date_hired", "Date Hired (YYYY-MM-DD)", true, LocalDate.now().toString()),
                combo("status", "Employment Status", true, "ACTIVE", "ON LEAVE", "RESIGNED"),
                foreignKey("department_id", "Department", true,
                    "SELECT department_id, department_name FROM departments ORDER BY department_name"),
                foreignKey("position_id", "Position", true,
                    "SELECT position_id, position_title FROM positions ORDER BY position_title")),
            List.of("e.employee_no", "e.first_name", "e.last_name", "d.department_name", "p.position_title"));
    }

    public static EntityConfig attendance() {
        return new EntityConfig("Attendance", "attendance", "attendance_id",
            "SELECT a.attendance_id AS ID, e.employee_no AS \"Employee No.\", "
            + "e.first_name || ' ' || e.last_name AS Employee, a.attendance_date AS \"Date\", "
            + "a.time_in AS \"Time In\", a.time_out AS \"Time Out\", a.status AS Status "
            + "FROM attendance a JOIN employees e ON a.employee_id = e.employee_id",
            List.of(foreignKey("employee_id", "Employee", true,
                    "SELECT employee_id, employee_no || ' - ' || first_name || ' ' || last_name FROM employees ORDER BY last_name, first_name"),
                date("attendance_date", "Attendance Date (YYYY-MM-DD)", true, LocalDate.now().toString()),
                time("time_in", "Time In (hh:mm + AM/PM)", false), time("time_out", "Time Out (hh:mm + AM/PM)", false),
                combo("status", "Attendance Status", true, "PRESENT", "LATE", "ABSENT", "ON LEAVE")),
            List.of("e.employee_no", "e.first_name", "e.last_name", "a.status"));
    }

    public static EntityConfig leaves() {
        return new EntityConfig("Leave Requests", "leave_requests", "leave_id",
            "SELECT l.leave_id AS ID, e.employee_no AS \"Employee No.\", "
            + "e.first_name || ' ' || e.last_name AS Employee, l.leave_type AS \"Leave Type\", "
            + "l.start_date AS \"Start Date\", l.end_date AS \"End Date\", l.reason AS Reason, "
            + "l.status AS Status, l.date_filed AS \"Date Filed\" "
            + "FROM leave_requests l JOIN employees e ON l.employee_id = e.employee_id",
            List.of(foreignKey("employee_id", "Employee", true,
                    "SELECT employee_id, employee_no || ' - ' || first_name || ' ' || last_name FROM employees ORDER BY last_name, first_name"),
                combo("leave_type", "Leave Type", true, "Vacation", "Sick", "Emergency", "Other"),
                date("start_date", "Start Date (YYYY-MM-DD)", true, LocalDate.now().toString()),
                date("end_date", "End Date (YYYY-MM-DD)", true, LocalDate.now().toString()),
                text("reason", "Reason", false),
                combo("status", "Status", true, "PENDING", "APPROVED", "REJECTED"),
                date("date_filed", "Date Filed (YYYY-MM-DD)", true, LocalDate.now().toString())),
            List.of("e.employee_no", "e.first_name", "e.last_name", "l.leave_type", "l.status", "l.reason"));
    }
}
