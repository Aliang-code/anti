package dna.top.dao;

import dna.entity.top.EnterpriseInfo;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.persistence.hibernate.AbstractDAOMethod;

import java.util.List;

public abstract class EnterpriseInfoDAO extends AbstractDAOMethod<EnterpriseInfo> {
    public EnterpriseInfoDAO() {
        super("");
        this.setEntity(EnterpriseInfo.class);
    }

    @DAOMethod(sql = "from EnterpriseInfo where code=:code and status=1")
    public abstract EnterpriseInfo getByCode(@DAOParam("code") String code);

    @DAOMethod(sql = "from EnterpriseInfo where status=1")
    public abstract List<EnterpriseInfo> findAllEnterprise();

    @DAOMethod(sql = "update EnterpriseInfo set eventAmount=eventAmount-:cost where code=:companyCode")
    public abstract Integer updateEventAmount(@DAOParam("cost") int cost, @DAOParam("companyCode") String companyCode);
}
