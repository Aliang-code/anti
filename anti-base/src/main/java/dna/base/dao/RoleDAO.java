package dna.base.dao;

import dna.entity.base.Role;
import dna.origins.annotation.DAOCount;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.origins.annotation.LikeType;
import dna.persistence.hibernate.AbstractDAOMethod;

import java.util.Date;
import java.util.List;

public abstract class RoleDAO extends AbstractDAOMethod<Role> {

    public RoleDAO(String cid) {
        super(cid);
        this.setEntity(Role.class);
    }

    @DAOMethod(sql = "from Role where status = 1", limit = 0)
    public abstract List<Role> findAllRoles();

    @DAOCount(name = "countRoleInfoByName")
    @DAOMethod(sql = "from Role r where name like :name and status <> 2")
    public abstract List<Role> findRoleInfoLikeName(@DAOParam(value = "name", like = LikeType.BOTH_PERCENT) String name, @DAOParam(start = true) int start, @DAOParam(limit = true) int limit);

    @DAOMethod(sql = "update Role set name=:name,description=:description,status=:status,updateTime=:updateTime where id=:id")
    public abstract Integer updateRoleInfoById(@DAOParam("id") int id, @DAOParam("name") String name, @DAOParam("description") String description, @DAOParam("status") Integer status, @DAOParam("updateTime")Date updateTime);

    @DAOMethod(sql = "update Role set status=:status,updateTime=:updateTime where id=:id")
    public abstract Integer updateStatusById(@DAOParam("id") int id, @DAOParam("status") int status, @DAOParam("updateTime") Date updateTime);

    @DAOMethod(sql = "from Role r where name = :name")
    public abstract List<Role> findRoleInfoByName (@DAOParam(value = "name") String name);

    @DAOMethod(sql = "from Role r where name = :name and id <> :id")
    public abstract List<Role> findRoleInfoByNameAndId(@DAOParam(value = "id") Integer id, @DAOParam(value = "name") String name);

    @DAOCount(name = "findRoleInfoByUserIdCount")
    @DAOMethod(sql = "select r from Role r,UserRole ur where r.id = ur.roleId and ur.userId = :userId")
    public abstract List<Role> findRoleInfoByUserId(@DAOParam("userId") Integer userId, @DAOParam(start = true) int start, @DAOParam(limit = true) int limit);

    @DAOMethod(sql = "select name from Role", limit = 0)
    public abstract List<String> findAllRoleNames();
}
