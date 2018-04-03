package dna.persistence.hibernate;

import dna.persistence.factory.AppContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SessionFactoryHolder {
    private static String primaryDB = "";
    private static Map<String, SessionFactory> sessionFactoryMap = new HashMap<>();

    private SessionFactoryHolder() {
    }

    public static boolean hasSessionFactory(String cid) {
        cid = StringUtils.isBlank(cid) ? primaryDB : cid.trim();
        return sessionFactoryMap != null && !sessionFactoryMap.isEmpty() && sessionFactoryMap.keySet().contains(cid) && sessionFactoryMap.get(cid) != null;
    }

    public static SessionFactory get(String cid) {
        cid = StringUtils.isBlank(cid) ? primaryDB : cid.trim();
        if (sessionFactoryMap == null || sessionFactoryMap.isEmpty() || !sessionFactoryMap.keySet().contains(cid)) {
            throw new NoSuchBeanDefinitionException(cid, "cannot get SessionFactory");
        }
        SessionFactory sessionFactory = sessionFactoryMap.get(cid);
        if (sessionFactory == null) {
            throw new NoSuchBeanDefinitionException(cid, "cannot get SessionFactory");
        }
        return sessionFactory;
    }

    public static Set<String> getCidSet(){
        return sessionFactoryMap.keySet();
    }

    public void init() throws NoSuchFieldException {
        LoggerFactory.getLogger(this.getClass()).info("inject sessionFactory...");
        Map<String, SessionFactory> sb = AppContextHolder.getBeansOfType(SessionFactory.class);
        sb.forEach((k, v) -> {
            String sfName = k.substring(k.indexOf("_") + 1, k.length());
            if (sessionFactoryMap.get(sfName) != null) {
                throw new BeanCreationException("the sessionFactory[" + k + "] can not create unique identity by '" + sfName + "'");
            }
            sessionFactoryMap.put(sfName, v);
        });
        if (sessionFactoryMap.isEmpty()) throw new BeanCreationException("sessionFactory suffixes is empty");
    }

    public String getPrimaryDB() {
        return primaryDB;
    }

    public void setPrimaryDB(String primaryDB) {
        SessionFactoryHolder.primaryDB = primaryDB;
    }
}
