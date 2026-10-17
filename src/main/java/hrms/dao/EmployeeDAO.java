package hrms.dao;

import hrms.ui.EntityConfigs;

/** DAO for employee records. */
public final class EmployeeDAO extends GenericDAO {
    public EmployeeDAO() { super(EntityConfigs.employees()); }
}
