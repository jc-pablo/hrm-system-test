# Human Resource Management System (HRMS)

NetBeans-ready Java desktop project aligned with the Java System Project Requirements and HRMS paper. It uses Java 17+, Swing/AWT, JDBC, and **Apache Derby embedded mode**. The database is local and is initialized by the application on first launch; no MySQL/MariaDB server or phpMyAdmin is used.

## Included interface and modules

- **High-contrast buttons and sidebar:** white text on dark navy/blue sidebar buttons, a strong selected-page indicator, explicit hover/pressed states, and clear dark text on pale secondary buttons.

- **Login screen:** blank username/password fields, show-password option, Sign In, Clear/Reset, and Exit. Demo credentials are documented below, not displayed under the login fields.
- **Dashboard:** live counters for employees, active staff, departments, pending leave, today attendance breakdown, recently added employees, and quick navigation.
- **Employees:** employee number, personal/contact data, hire date, employment status, department and position assignment.
- **Departments and Positions:** CRUD forms, table views, search, refresh, and duplicate checking through database constraints.
- **Attendance:** employee, date, time in/out, and status. Enter the time as `hh:mm` and select the AM or PM checkbox for each time. Overnight shifts are supported: if Time Out is earlier than Time In, the system treats Time Out as occurring on the following calendar day. The attendance date remains the shift start date, and table/report rows mark the time-out with `(next day)`. Duplicate employee/date rows are prevented by a database unique constraint.
- **Leave Requests:** leave type, date range, reason, filing date, and review status. HR Staff can file and maintain requests while Administrator-only review controls update approval status.
- **User Accounts:** Administrator-only account management with roles `ADMIN` and `HR_STAFF`.
- **Reports:** Employee Directory, Department Headcount, Attendance Register, Leave Register, and Pending Leave Requests; reports display in a `JTable` and can be exported as CSV.
- **Security/data integrity:** password fields, salted PBKDF2-HMAC-SHA256 hashes, prepared statements, field/date/time/email validation, role permissions, delete confirmation, primary/foreign/unique/check constraints, and readable error messages.

## Matching documentation

The matching paper is included in `docs/HRMS_Project_Documentation_Derby.pdf` and `docs/HRMS_Project_Documentation_Derby.docx`. It describes this embedded Apache Derby build, the actual modules/roles, database relationships, and the current test matrix. Runtime test outcomes are left pending until they are performed.

## Requirements

- JDK 17 or newer.
- Apache NetBeans with Maven support.
- Internet access on first Maven build so Maven can download Apache Derby and compiler/plugin dependencies.

Apache Derby 10.16.1.1 is selected because it supports Java 17 and later. See the official Derby release notes and Maven Central artifact page linked in `docs/references.md`.

## Open and run in NetBeans

1. Extract this ZIP.
2. In NetBeans, select **File > Open Project** and choose the `hrms_netbeans` folder that contains `pom.xml`.
3. Allow Maven to resolve the dependency `org.apache.derby:derby:10.16.1.1`.
4. Right-click the project and choose **Run**. The NetBeans Maven action and `hrms.Main` are configured.
5. On the first run, the application creates a local folder named `hrmsdb` in the project's working directory, creates the six tables, and seeds demo accounts/reference records.

If the project folder is not writable, choose another database location by setting the Java system property `-Dhrms.db.path=/absolute/path/hrmsdb` or environment variable `HRMS_DB_PATH`. Keep the database path stable so the app opens the same database each time.


## Login screen version note (No Demo Access panel)

The login screen intentionally contains only the username/password fields, Show password, Sign In, Clear / Reset, and Exit. It has **no Demo Access panel, no demo credentials beneath the fields, and no footer beneath the buttons**. Demo credentials remain in this README and the database initializer only for testing.

If your running application still shows a Demo Access box, it is loading an older extracted project/build. Stop the running application, extract this ZIP to a **new folder** (do not extract over an older copy), close the old project in NetBeans, and use **File > Open Project** to open this new `hrms_netbeans` folder. Then right-click the project and select **Clean and Build**, followed by **Run**. The source of truth is `src/main/java/hrms/ui/LoginFrame.java`; its `buildScreen()` method intentionally adds no Demo Access component.

## Demo credentials

| Role | Username | Password |
|---|---|---|
| Administrator | `admin` | `admin123` |
| HR Staff | `hrstaff` | `hr123` |

The accounts are added only if the corresponding usernames do not already exist. These are prototype credentials: change them through **User Accounts** before using real information.

## First-use workflow

1. Sign in as `admin`.
2. Review **Departments** and **Positions**; starter entries are included.
3. Add an employee and assign a department/position.
4. Add attendance and leave requests for that employee.
5. Review/approve leave as Administrator.
6. Open **Reports** to view a report or export the displayed result to CSV.
7. Create named HR Staff accounts and test the restricted view using `hrstaff`.

## Database details

- Engine: Apache Derby embedded JDBC driver.
- Default URL: `jdbc:derby:hrmsdb;create=true` (the Java connection class resolves this relative database path from the program's working directory).
- Schema: `users`, `departments`, `positions`, `employees`, `attendance`, `leave_requests`.
- Startup class: `hrms.util.DatabaseSetup` creates missing tables and seeds demo/default records. The exact matching DDL is in `database/hrms_schema.sql` for reference/manual setup on an empty database. **Do not execute that DDL against a database already initialized by the app**, because it would recreate existing tables.

Derby embedded mode is not managed through phpMyAdmin. A local Derby database is stored in a database directory rather than as a MySQL database schema. The application's JDBC URL and schema are already matched to the DDL above.

## Roles and permissions

- **Administrator:** all HR modules, user accounts, delete operations, and leave approval/rejection.
- **HR Staff:** employees, departments, positions, attendance, leave filing/editing without changing approval status, and reports. Delete operations and user-account management are restricted.

Permissions are enforced in the Java action handlers in addition to hiding restricted navigation.

## Scope and limitations

Included: local desktop HR record management, authentication, CRUD/search, reports, and related-table integrity. Excluded: payroll/tax computations, recruitment, performance review, biometric hardware, employee web self-service, and network/multi-user deployment. Use synthetic data for class demonstration. This project has not been represented as production-ready.

## Build and test status

The Java sources are designed for compilation with JDK 17. `docs/testing-checklist.md` contains a runtime verification matrix. Actual runtime outcomes must be recorded after launching against Derby in NetBeans; compilation alone does not prove a live database operation passed.
