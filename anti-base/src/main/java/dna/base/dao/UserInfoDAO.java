package dna.base.dao;

import dna.entity.base.User;
import dna.entity.base.UserInfo;
import dna.origins.annotation.DAOCount;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.DAOParam;
import dna.origins.annotation.LikeType;
import dna.persistence.hibernate.AbstractDAOMethod;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangxw on 2017/8/15.
 * 用户管理数据操作
 */
public abstract class UserInfoDAO extends AbstractDAOMethod<User> {

    public UserInfoDAO(String cid) {
        super(cid);
        this.setEntity(User.class);
    }

    @DAOMethod(sql = "update User set status=:status,updateTime=:updateTime where id=:id")
    public abstract Integer updateStatusById(@DAOParam("id") int id, @DAOParam("status") int status, @DAOParam("updateTime") Date updateTime);

    @DAOMethod(sql = "update User set realName = :realName, status = :status, updateTime = :updateTime where id = :id")
    public abstract Integer updateUserInfo(@DAOParam("id") int id, @DAOParam("realName") String realName, @DAOParam("status") Integer status, @DAOParam("updateTime") Date updateTime);

    @DAOMethod(sql = "update User set salt = :salt, password = :password, updateTime=:updateTime where id=:id")
    public abstract Integer updatePasswordById(@DAOParam("id") int id, @DAOParam("salt") String salt, @DAOParam("password") String password, @DAOParam("updateTime") Date updateTime);

    @DAOMethod(sql = "select new dna.entity.base.UserInfo(u) from User u")
    public abstract List<UserInfo> findAllUserInfo(@DAOParam(start = true) int start, @DAOParam(limit = true) int limit);

    @DAOCount(name = "countByUserName")
    @DAOMethod(sql = "select new dna.entity.base.UserInfo(u) from User u,UserRole ur where u.id = ur.userId and ur.roleId = :roleId and u.userName like :userName and u.realName like :realName")
    public abstract List<UserInfo> findUserInfoByUserName(@DAOParam(value = "userName", like = LikeType.BOTH_PERCENT) String userName, @DAOParam(value = "realName", like = LikeType.BOTH_PERCENT) String realName, @DAOParam(value = "roleId") Integer roleId, @DAOParam(start = true) int start, @DAOParam(limit = true) int limit);

    @DAOCount(name = "findUserInfoByRoleIdCount")
    @DAOMethod(sql = "select new dna.entity.base.UserInfo(u) from User u,UserRole ur where u.id = ur.userId and ur.roleId = :roleId and u.id <> :currentUserId")
    public abstract List<UserInfo> findUserInfoByRoleId(@DAOParam("roleId") Integer roleId, @DAOParam("currentUserId") Integer currentUserId, @DAOParam(start = true) int start, @DAOParam(limit = true) int limit);

    @DAOCount(name = "findUserInfoCount")
    public List<UserInfo> findUserInfo(String userName, String realName, Integer roleId, @DAOParam(start = true) int start, @DAOParam(limit = true) int limit, Integer currentUserId) {

        Map<String, Object> params =  new HashMap<>();
        StringBuffer hql = new StringBuffer();

        hql.append("select new dna.entity.base.UserInfo(u) ");
        hql.append("from User u,UserRole ur ");
        hql.append("where 1 = 1 ");
        hql.append("and u.id = ur.userId ");
        hql.append("and ur.status = 1 ");
        if (currentUserId != null) {
            hql.append("and u.id <> :currentUserId ");
            params.put("currentUserId", currentUserId);
        }
        if (!"".equals(userName) && userName != null) {
            hql.append("and u.userName like :userName ");
            params.put("userName", "%" + userName + "%");
        }
        if (!"".equals(realName) && realName != null) {
            hql.append("and u.realName like :realName ");
            params.put("realName", "%" + realName + "%");
        }
        if (roleId != null) {
            hql.append("and ur.roleId = :roleId ");
            params.put("roleId", roleId);
        }

        return executeHQLForPage(hql, start, limit, params);

    }

    @DAOMethod(sql = "update User set password=:password,salt=:salt,updateTime=:updateTime where id=:id")
    public abstract int updateUserPwd(@DAOParam("id") Integer id, @DAOParam("password") String password, @DAOParam("salt") String salt, @DAOParam("updateTime") Date updateTime);

    @DAOMethod(sql = "select new dna.entity.base.UserInfo(u) from User u where id=:id")
    public abstract UserInfo getUserInfoById(@DAOParam("id") Integer id);

    @DAOMethod(sql = "select new dna.entity.base.UserInfo(u) from User u where userName =:userName")
    public abstract UserInfo getUserInfoByUserName(@DAOParam("userName") String userName);

}
