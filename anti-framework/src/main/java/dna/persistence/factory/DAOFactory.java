package dna.persistence.factory;

import dna.origins.annotation.*;
import dna.origins.commons.SerializableEntity;
import dna.origins.commons.ServiceException;
import dna.persistence.hibernate.AbstractDAOMethod;
import dna.persistence.regulate.DictionaryService;
import dna.persistence.shiro.UserRoleToken;
import dna.persistence.template.SQLQueryInfo;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by Aliang on 2017/7/28.
 */
public class DAOFactory {
    private static final Logger logger = LoggerFactory.getLogger(DAOFactory.class);
    private static ConcurrentHashMap<String, AbstractDAOMethod> daoMap = new ConcurrentHashMap<>();
    private static final int MODIFIER_ABSTRACT = 1024;

    private DAOFactory() {
    }

    public static <T> T getDAO(final Class<T> clazz) {
        if (daoMap.get(clazz.getSimpleName()) == null) {
            try {
                T var = createDAO(clazz);
                daoMap.put(clazz.getSimpleName(), (AbstractDAOMethod) var);
                return var;
            } catch (Throwable e) {
                throw new ServiceException(e, ServiceException.DAO_NOT_FOUND, "can not get DAO[" + clazz.getSimpleName() + "]");
            }
        } else {
            return (T) daoMap.get(clazz.getSimpleName());
        }
    }

    public static <T> T getDAOByUrt(final Class<T> clazz) {
        UserRoleToken urt = UserRoleToken.getCurrentUrt();
        Assert.notNull(urt, "userRoleToken is required");
        String cid = urt.getCompanyCode();
        return getDAOByCid(clazz, cid);
    }

    public static <T> T getDAOByCid(final Class<T> clazz, String cid) {
        cid = cid == null ? UserRoleToken.CID_BASE : cid.trim();
        if (daoMap.get(clazz.getSimpleName() + cid) == null) {
            try {
                T var = createDAOByCid(clazz, cid);
                daoMap.put(clazz.getSimpleName() + cid, (AbstractDAOMethod) var);
                return var;
            } catch (Throwable e) {
                throw new ServiceException(e, ServiceException.DAO_NOT_FOUND, "can not get DAO[" + clazz.getSimpleName() + "] by cid[" + cid + "]");
            }
        } else {
            return (T) daoMap.get(clazz.getSimpleName() + cid);
        }
    }

    private static <T> T createDAOByCid(final Class<T> tClazz, final String cid) throws Throwable {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(tClazz);
        T dao = (T) factory.create(new Class[]{String.class}, new Object[]{cid}, generateHandler(tClazz));
        return dao;
    }

    private static <T> T createDAO(final Class<T> tClazz) throws Throwable {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(tClazz);
        T dao = (T) factory.create(new Class[0], new Object[0], generateHandler(tClazz));
        return dao;
    }

    @SuppressWarnings("unchecked")
    private static MethodHandler generateHandler(final Class tClazz) {
        return new MethodHandler() {
            @Override
            public Object invoke(Object self, Method tMethod, Method proceed, Object[] args) throws Throwable {
                Object result;
                AbstractDAOMethod tmpObj = (AbstractDAOMethod) self;
                Pattern p = Pattern.compile("^(update|save|delete|insert)(?!Batch$)");
                if (p.matcher(tMethod.getName()).find()) {
                    DictionaryService dictionaryService = AppContextHolder.getBean("dictionaryService", DictionaryService.class);
                    if (dictionaryService.hasDicEntity(tmpObj.getEntity())) {
                        Set<String> keySet = dictionaryService.getEntityKeys(tmpObj.getEntity());
                        dictionaryService.updateDictionary(keySet, tmpObj.getCid());
                    }
                }
                if (tMethod.getDeclaringClass() == AbstractDAOMethod.class) {
                    result = proceed.invoke(self, args);
                    if (tMethod.isAnnotationPresent(DAOMethod.class)) {
                        result = injectCidForEntity(result, tmpObj.getCid());
                    }
                    return result;
                }
                int modifier = tMethod.getModifiers();
                if (tMethod.isAnnotationPresent(DAOCount.class)) {
                    List<Object> countArgList = new ArrayList<>();
                    Annotation[][] ans = tMethod.getParameterAnnotations();
                    Map<String, Object> countParams = new HashMap<>();
                    for (int i = 0; i < ans.length; i++) {
                        Object var = args[i];
                        boolean isCountParam = true;
                        for (int j = 0; j < ans[i].length; j++) {
                            if (ans[i][j].annotationType().equals(DAOParam.class)) {
                                DAOParam param = (DAOParam) ans[i][j];
                                if (param.limit() || param.start()) {
                                    isCountParam = false;
                                    continue;
                                } else if ((MODIFIER_ABSTRACT & modifier) == MODIFIER_ABSTRACT) {
                                    countParams.put(param.value(), tMethod.getParameterTypes()[i] == String.class ? (
                                            param.like() == LikeType.NONE ? var :
                                                    param.like() == LikeType.BOTH_PERCENT ? (var == null ? "%%" : "%" + var + "%") : var == null ? "%" :
                                                            param.like() == LikeType.LEFT_PERCENT ? "%" + var :
                                                                    param.like() == LikeType.RIGHT__PERCENT ? var + "%" : var) : var);
                                }
                                break;
                            }
                        }
                        if (isCountParam) {
                            countArgList.add(var);
                        }
                    }

                    DAOCount daoCount = tMethod.getAnnotation(DAOCount.class);
                    String countName = StringUtils.isEmpty(daoCount.name()) ? tMethod.getName() : daoCount.name();
                    QueryType queryType = daoCount.queryType();
                    Object[] countArgs = countArgList.toArray();
                    if (StringUtils.isBlank(countName)) {
                        throw new IllegalArgumentException("@DAOCount name can not be space");
                    }
                    String countKey = AbstractDAOMethod.generateCountName(countName, countArgs);
                    if (!tmpObj.hasCountName(countKey)) {
                        String countSql = daoCount.sql();
                        if (StringUtils.isBlank(countSql)) {
                            if ((MODIFIER_ABSTRACT & modifier) == MODIFIER_ABSTRACT) {
                                if (tMethod.isAnnotationPresent(DAOMethod.class)) {
                                    DAOMethod daoMethod = tMethod.getAnnotation(DAOMethod.class);
                                    if (StringUtils.isBlank(daoMethod.sql())) {
                                        throw new NullPointerException("DAO[" + tClazz.getSimpleName() + "] method[" + tMethod.getName() + "] do not have sql for @DAOCount");
                                    }
                                    countSql = daoMethod.sql();
                                    SQLQueryInfo queryInfo = AbstractDAOMethod.generateQueryInfo(countSql, countParams, QueryType.HQL, tMethod, tmpObj.getSessionFactory());
                                    tmpObj.setQueryInfo(countKey, queryInfo);
                                } else {
                                    throw new NullPointerException("DAO[" + tClazz.getSimpleName() + "] method[" + tMethod.getName() + "] do not have sql for @DAOCount");
                                }
                            } else {
                                synchronized (Thread.currentThread().getName().intern()) {
                                    tmpObj.setCurrentCountName(countKey);
                                    result = proceed.invoke(self, args);
                                    if (!tmpObj.hasCountName(tmpObj.getCurrentCountName())) {
                                        throw new NoSuchElementException("can not set queryInfo for method[" + tClazz.getSimpleName() + "." + tMethod.getName() + "]");
                                    }
                                    tmpObj.setCurrentCountName(null);
                                    return injectCidForEntity(result, tmpObj.getCid());
                                }
                            }
                        } else {
                            tmpObj.setQueryInfo(countKey, new SQLQueryInfo(countSql, countParams, queryType));
                        }
                    }
                }
                if (tMethod.isAnnotationPresent(DAOMethod.class)) {
                    DAOMethod daoMethod = tMethod.getAnnotation(DAOMethod.class);
                    String hql = daoMethod.sql();
                    if (StringUtils.isBlank(hql)) {
                        throw new NullPointerException("DAO[" + tClazz.getSimpleName() + "] method[" + tMethod.getName() + "] do not support by @DAOMethod");
                    }
                    int limit = daoMethod.limit();
                    int start = daoMethod.start();
                    Annotation[][] ans = tMethod.getParameterAnnotations();
                    Map<String, Object> daoParams = new HashMap<>();
                    for (int i = 0; i < ans.length; i++) {
                        for (int j = 0; j < ans[i].length; j++) {
                            if (ans[i][j].annotationType().equals(DAOParam.class)) {
                                DAOParam param = (DAOParam) ans[i][j];
                                Object var = args[i];
                                if (param.limit()) {
                                    limit = (int) var;
                                } else if (param.start()) {
                                    start = (int) var;
                                } else {
                                    daoParams.put(param.value(), tMethod.getParameterTypes()[i] == String.class ? (
                                            param.like() == LikeType.NONE ? var :
                                                    param.like() == LikeType.BOTH_PERCENT ? (var == null ? "%%" : "%" + var + "%") : var == null ? "%" :
                                                            param.like() == LikeType.LEFT_PERCENT ? "%" + var :
                                                                    param.like() == LikeType.RIGHT__PERCENT ? var + "%" : var) : var);
                                }
                                break;
                            }
                        }

                    }
                    final int fStart = start;
                    final int fLimit = limit;
                    if (tMethod.getName().startsWith("get")) {
                        result = tmpObj.getHibernateTemplate().execute(new HibernateCallback() {
                            @Override
                            public Object doInHibernate(Session session) throws HibernateException {
                                Object result;
                                Query query = session.createQuery(hql);
                                AbstractDAOMethod.setParameterMap(query,daoParams);
                                result = query.uniqueResult();
                                return result;
                            }
                        });
                    } else if (tMethod.getName().startsWith("find")) {
                        result = tmpObj.getHibernateTemplate().execute(new HibernateCallback() {
                            @Override
                            public Object doInHibernate(Session session) throws HibernateException {
                                Object list;
                                Query query = session.createQuery(hql);
                                AbstractDAOMethod.setParameterMap(query,daoParams);
                                query.setFirstResult(fStart);
                                if (fLimit != 0) {
                                    query.setMaxResults(fLimit);
                                }
                                list = query.list();
                                return list;
                            }
                        });
                    } else if (tMethod.getName().startsWith("update")) {
                        result = tmpObj.getHibernateTemplate().execute(new HibernateCallback<Integer>() {
                            @Override
                            public Integer doInHibernate(Session session) throws HibernateException {
                                Query query = session.createQuery(hql);
                                AbstractDAOMethod.setParameterMap(query,daoParams);
                                return query.executeUpdate();
                            }
                        });
                    } else {
                        throw new ServiceException(ServiceException.SERVICE_ERROR, "DAO[" + tClazz.getSimpleName() + "] method[" + tMethod.getName() + "] do not support by @DAOMethod");
                    }
                } else {
                    result = proceed.invoke(self, args);
                }
                return injectCidForEntity(result, tmpObj.getCid());
            }
        };
    }

    private static Object injectCidForEntity(Object result, String cid) {
        if (result instanceof Collection) {
            ((Collection) result).forEach(r -> {
                if (r instanceof SerializableEntity) {
                    ((SerializableEntity) r).setCid(cid);
                }
            });
        }
        if (result instanceof Map) {
            ((Map) result).values().forEach(v -> {
                if (v instanceof SerializableEntity) {
                    ((SerializableEntity) v).setCid(cid);
                }
            });
        }
        if (result instanceof SerializableEntity) {
            ((SerializableEntity) result).setCid(cid);
        }
        return result;
    }
}
