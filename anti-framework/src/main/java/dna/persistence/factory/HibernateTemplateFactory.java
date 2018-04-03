package dna.persistence.factory;

import dna.origins.commons.DatabaseType;
import dna.persistence.hibernate.SessionFactoryHolder;
import dna.persistence.shiro.UserRoleToken;
import dna.persistence.template.EnvConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import javax.validation.constraints.NotNull;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class HibernateTemplateFactory {
    private static final Logger logger = LoggerFactory.getLogger(HibernateTemplateFactory.class);
    private static final String TEMP_PREFIX = "HibernateTemplate_";
    public static ConcurrentHashMap<String, HibernateTemplate> tempMap = new ConcurrentHashMap<>();

    private HibernateTemplateFactory() {
    }

    public static HibernateTemplate getTempByUrt() {
        UserRoleToken urt = UserRoleToken.getCurrentUrt();
        Assert.notNull(urt, "userRoleToken is required");
        return getTempByCid(urt.getCompanyCode());
    }

    public static HibernateTemplate getTempByCid(String cid) {
        cid = cid == null ? "" : cid.trim();
        HibernateTemplate hibernateTemplate = tempMap.get(TEMP_PREFIX + cid);
        if (hibernateTemplate == null) {
            try {
                HibernateTemplate var = createTemp(cid);
                tempMap.put(TEMP_PREFIX + cid, var);
                return var;
            } catch (Throwable e) {
                logger.error("can not get HibernateTemplate by cid[{}]", cid, e);
                return null;
            }
        } else {
            return hibernateTemplate;
        }
    }

    private static HibernateTemplate createTemp(String cid) {
        return new HibernateTemplate(SessionFactoryHolder.get(cid));
    }

    public static LocalSessionFactoryBean generateSessionFactory(@NotNull DatabaseType type, DataSource dataSourceBean) {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        Properties hibernateProperties = new Properties();
        hibernateProperties.setProperty("hibernate.dialect", type.getDialect());
        if (EnvConfig.showSql != null && EnvConfig.showSql) {
            hibernateProperties.setProperty("hibernate.show_sql", "true");
        }
        sessionFactoryBean.setDataSource(dataSourceBean);
        sessionFactoryBean.setHibernateProperties(hibernateProperties);
        sessionFactoryBean.setPackagesToScan("dna.**.entity");
        return sessionFactoryBean;
    }
}
