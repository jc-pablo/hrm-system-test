package hrms.dao;

import hrms.ui.EntityConfigs;

/** DAO for department records. */
public final class DepartmentDAO extends GenericDAO {
    public DepartmentDAO() { super(EntityConfigs.departments()); }
}
