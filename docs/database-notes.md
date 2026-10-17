# Apache Derby database notes

The application uses the embedded Derby JDBC driver and a local database named `hrmsdb`. Default JDBC URL: `jdbc:derby:hrmsdb;create=true`.

On the first start, `DatabaseSetup.initialize()` creates these tables if missing:

- `users`: administrator/HR Staff accounts and salted PBKDF2-HMAC-SHA256 password hashes.
- `departments`: department names and descriptions.
- `positions`: position titles and descriptions.
- `employees`: employee personal and employment information; references one department and one position.
- `attendance`: daily employee attendance; unique `(employee_id, attendance_date)` prevents duplicates.
- `leave_requests`: leave type, date range, filing date, and pending/approved/rejected status.

Schema integrity includes primary keys, unique username/employee number/department name/position title, foreign keys, and check constraints for roles, statuses, and leave date order. Attendance intentionally has no same-day time-order check so that overnight shifts can be recorded.

`database/hrms_schema.sql` documents the matching DDL. The program creates the schema automatically and generates initial demo password hashes with a random salt. Derby embedded mode is not a MySQL server and cannot be opened by phpMyAdmin.

## Derby database location

By default, Derby writes the `hrmsdb` database directory beneath the application's working directory. The directory should not be committed or packaged as source. Override the location with Java system property `hrms.db.path` or environment variable `HRMS_DB_PATH` if needed.


### Overnight attendance shifts

`attendance_date` stores the date on which the shift starts. The `time_in` and `time_out` columns are SQL TIME values stored in 24-hour form. The UI asks users for a 12-hour time plus AM/PM selection. If `time_out` is earlier than `time_in`, display logic treats it as the following calendar day and labels it `(next day)`. The former `CK_ATTENDANCE_TIME` constraint is removed on startup from older databases so overnight shifts can be stored.
