package hrms.dao;

import hrms.ui.EntityConfigs;

/** DAO for position records. */
public final class PositionDAO extends GenericDAO {
    public PositionDAO() { super(EntityConfigs.positions()); }
}
