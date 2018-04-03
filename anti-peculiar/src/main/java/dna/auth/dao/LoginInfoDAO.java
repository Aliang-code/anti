package dna.auth.dao;

import dna.entity.base.Permission;
import dna.entity.base.Role;
import dna.entity.base.User;
import dna.persistence.factory.HibernateTemplateFactory;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.stereotype.Repository;

import java.util.List;

import static dna.persistence.shiro.UserRoleToken.CID_BASE;
import static dna.persistence.shiro.UserRoleToken.CNAME_BASE;

@Repository
public class LoginInfoDAO {
    public static final Logger logger = LoggerFactory.getLogger(LoginInfoDAO.class);

    public String getCompanyName(String cid) {
        if (StringUtils.isBlank(cid) || cid == CID_BASE) {
            return CNAME_BASE;
        }
        String companyName = HibernateTemplateFactory.getTempByCid(null).execute(session -> {
            String hql = "select name from EnterpriseInfo where code=:code and status=1";
            Query q = session.createQuery(hql);
            q.setParameter("code", cid);
            return (String) q.uniqueResult();
        });
        if (StringUtils.isBlank(companyName)) {
            return null;
        } else {
            return companyName;
        }
    }

    public User findPwdInfoByUserName(String userName, String cid) {
        return HibernateTemplateFactory.getTempByCid(cid).execute(new HibernateCallback<User>() {
            @Override
            public User doInHibernate(Session session) throws HibernateException {
                String hql = "from User where userName=:userName and status=1";
                Query q = session.createQuery(hql);
                q.setParameter("userName", userName);
                return (User) q.uniqueResult();
            }
        });
    }

    public List<Role> findRoleByUserName(String userName, String cid) {
        return HibernateTemplateFactory.getTempByCid(cid).execute(new HibernateCallback<List<Role>>() {
            @Override
            public List<Role> doInHibernate(Session session) throws HibernateException {
                String hql = new String("select r from Role r,UserRole ur,User u where u.userName=:userName and u.status=1 and r.status<>0 and r.id=ur.roleId and ur.userId=u.id");
                Query q = session.createQuery(hql);
                q.setParameter("userName", userName);
                return q.list();
            }
        });
    }

    public List<Permission> findPermissionByRoleId(List<Integer> roleIds, String cid) {
        return HibernateTemplateFactory.getTempByCid(cid).execute(new HibernateCallback<List<Permission>>() {
            @Override
            public List<Permission> doInHibernate(Session session) throws HibernateException {
                String hql = new String("select distinct(p) from Permission p,RolePms rp where rp.roleId in (:roleIds) and rp.status=1 and p.id=rp.permissionId and p.status=1");
                Query q = session.createQuery(hql);
                q.setParameterList("roleIds", roleIds);
                return q.list();
            }
        });
    }
}
