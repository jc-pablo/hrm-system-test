package hrms.dao;

import hrms.ui.EntityConfigs;

/** DAO for leave requests. */
public final class LeaveDAO extends GenericDAO {
    public LeaveDAO() { super(EntityConfigs.leaves()); }
}
