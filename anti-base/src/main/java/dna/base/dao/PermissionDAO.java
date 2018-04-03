package dna.base.dao;

import dna.entity.base.Permission;
import dna.origins.annotation.DAOMethod;
import dna.persistence.hibernate.AbstractDAOMethod;

import java.util.List;

public abstract class PermissionDAO extends AbstractDAOMethod<Permission> {

    public PermissionDAO(String cid) {
        super(cid);
        this.setEntity(Permission.class);
    }

    @DAOMethod(sql = "from Permission where status=1", limit = 0)
    public abstract List<Permission> findAllPermissions();

    @DAOMethod(sql = "select name from Permission", limit = 0)
    public abstract List<String> findAllPermissionNames();
}
