package dna.top.dao;

import dna.entity.top.WxAccountInfo;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.persistence.hibernate.SingleSourceDAOMethod;

public abstract class WxAccountInfoDAO extends SingleSourceDAOMethod<WxAccountInfo> {
    public WxAccountInfoDAO() {
        super();
        setEntity(WxAccountInfo.class);
    }

    @DAOMethod(sql = "from WxAccountInfo where appId=:appId and status>0")
    public abstract WxAccountInfo getByAppId(@DAOParam("appId") String appId);
}
