package dna.base.dao;

import dna.entity.base.RolePms;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.persistence.hibernate.AbstractDAOMethod;

import java.util.List;

public abstract class RolePmsDAO extends AbstractDAOMethod<RolePms> {

    public RolePmsDAO(String cid) {
        super(cid);
        this.setEntity(RolePms.class);
    }

    @DAOMethod(sql = "select permissionId from RolePms where roleId=:roleId and status = 1", limit = 0)
    public abstract List<Integer> findPermissionIdByRoleId(@DAOParam("roleId") Integer roleId);

    @DAOMethod(sql = "select id from RolePms where roleId=:roleId", limit = 0)
    public abstract List<Integer> findIdByRoleId(@DAOParam("roleId") Integer roleId);


}
