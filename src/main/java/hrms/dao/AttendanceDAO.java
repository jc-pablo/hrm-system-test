package hrms.dao;

import hrms.ui.EntityConfigs;

/** DAO for attendance records. */
public final class AttendanceDAO extends GenericDAO {
    public AttendanceDAO() { super(EntityConfigs.attendance()); }
}
