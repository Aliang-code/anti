package dna.top.dao;

import dna.entity.top.EnterpriseConfiguration;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.persistence.hibernate.AbstractDAOMethod;

import java.util.Date;

public abstract class EnterpriseConfigurationDAO extends AbstractDAOMethod<EnterpriseConfiguration> {
    public EnterpriseConfigurationDAO() {
        super("");
        this.setEntity(EnterpriseConfiguration.class);
    }

    @Override
    public <B> B save(EnterpriseConfiguration v) {

        if (v.getCreateTime() == null) {
            v.setCreateTime(new Date());
        }
        return super.save(v);
    }

    @Override
    public void update(EnterpriseConfiguration v) {

        v.setUpdateTime(new Date());
        super.update(v);
    }

    @DAOMethod(sql = "from EnterpriseConfiguration where companyCode = :companyCode")
    public abstract EnterpriseConfiguration getByCompanyCode(@DAOParam("companyCode") String companyCode);
}
