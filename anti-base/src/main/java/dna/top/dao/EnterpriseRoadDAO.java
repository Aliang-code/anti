package dna.top.dao;

import dna.entity.top.EnterpriseRoad;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.persistence.hibernate.AbstractDAOMethod;

import java.util.List;

public abstract class EnterpriseRoadDAO extends AbstractDAOMethod<EnterpriseRoad> {
    public EnterpriseRoadDAO() {
        super("");
        this.setEntity(EnterpriseRoad.class);
    }

    @DAOMethod(sql = "from EnterpriseRoad where code=:code and status=1")
    public abstract EnterpriseRoad getByCode(@DAOParam("code") String code);

    @DAOMethod(sql = "from EnterpriseRoad where status=1")
    public abstract List<EnterpriseRoad> findAllEnterpriseRoad();
}
