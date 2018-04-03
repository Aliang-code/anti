package dna.base.dao;

import dna.entity.base.UserRole;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.persistence.hibernate.AbstractDAOMethod;

import java.util.List;

public abstract class UserRoleDAO extends AbstractDAOMethod<UserRole> {
    public UserRoleDAO(String cid) {
        super(cid);
        this.setEntity(UserRole.class);
    }

    /*public List<Integer> findRoleIdByUserId(Integer userId){
        String hql="select roleId From userRole where userId = ?0";
        return (List<Integer>)hibernateTemplate.find(hql,userId);
    }*/

    @DAOMethod(sql = "select roleId from UserRole where userId=:userId and status = 1", limit = 0)
    public abstract List<Integer> findRoleIdByUserId(@DAOParam("userId") Integer userId);

    @DAOMethod(sql = "select userId from UserRole where roleId = :roleId and status = 1", limit = 0)
    public abstract List<Integer> findUserIdByRoleId(@DAOParam("roleId") Integer roleId);

    @DAOMethod(sql = "from UserRole where userId = :userId")
    public abstract List<UserRole> findUserRoleListByUserId(@DAOParam("userId") Integer userId);
}
