-- HUMAN RESOURCE MANAGEMENT SYSTEM - APACHE DERBY SCHEMA
-- Database target: jdbc:derby:hrmsdb;create=true
-- This script is provided for reference/manual schema setup on a NEW empty Derby database.
-- The application creates these tables automatically at first launch; do not run this script
-- over a database that already contains the HRMS tables.
-- Demo user password hashes are generated with salted PBKDF2 by DatabaseSetup.java on startup.

CREATE TABLE users (
    user_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    username VARCHAR(30) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(80) NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'HR_STAFF'))
);

CREATE TABLE departments (
    department_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    department_name VARCHAR(60) NOT NULL,
    description VARCHAR(150),
    CONSTRAINT pk_departments PRIMARY KEY (department_id),
    CONSTRAINT uq_departments_name UNIQUE (department_name)
);

CREATE TABLE positions (
    position_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    position_title VARCHAR(60) NOT NULL,
    description VARCHAR(150),
    CONSTRAINT pk_positions PRIMARY KEY (position_id),
    CONSTRAINT uq_positions_title UNIQUE (position_title)
);

CREATE TABLE employees (
    employee_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    employee_no VARCHAR(15) NOT NULL,
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(40) NOT NULL,
    birth_date DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    email VARCHAR(80),
    phone VARCHAR(20),
    address VARCHAR(150),
    date_hired DATE NOT NULL,
    status VARCHAR(15) NOT NULL,
    department_id INTEGER NOT NULL,
    position_id INTEGER NOT NULL,
    CONSTRAINT pk_employees PRIMARY KEY (employee_id),
    CONSTRAINT uq_employees_no UNIQUE (employee_no),
    CONSTRAINT ck_employees_status CHECK (status IN ('ACTIVE', 'ON LEAVE', 'RESIGNED')),
    CONSTRAINT fk_employees_department FOREIGN KEY (department_id) REFERENCES departments(department_id),
    CONSTRAINT fk_employees_position FOREIGN KEY (position_id) REFERENCES positions(position_id)
);

-- attendance_date is the date the shift starts. If time_out is earlier than time_in,
-- the shift is considered to end on the following calendar day.
CREATE TABLE attendance (
    attendance_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    employee_id INTEGER NOT NULL,
    attendance_date DATE NOT NULL,
    time_in TIME,
    time_out TIME,
    status VARCHAR(15) NOT NULL,
    CONSTRAINT pk_attendance PRIMARY KEY (attendance_id),
    CONSTRAINT uq_attendance_employee_date UNIQUE (employee_id, attendance_date),
    CONSTRAINT ck_attendance_status CHECK (status IN ('PRESENT', 'LATE', 'ABSENT', 'ON LEAVE')),
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

CREATE TABLE leave_requests (
    leave_id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    employee_id INTEGER NOT NULL,
    leave_type VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(200),
    status VARCHAR(15) NOT NULL DEFAULT 'PENDING',
    date_filed DATE NOT NULL,
    CONSTRAINT pk_leave_requests PRIMARY KEY (leave_id),
    CONSTRAINT ck_leave_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_leave_dates CHECK (end_date >= start_date),
    CONSTRAINT fk_leave_employee FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);
