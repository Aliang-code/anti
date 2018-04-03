package dna.persistence.hibernate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dna.origins.annotation.DAOCount;
import dna.origins.annotation.DAOMethod;
import dna.origins.annotation.QueryType;
import dna.origins.util.encrypt.HashEncryptUtils;
import dna.origins.util.toolbar.DateConversion;
import dna.persistence.template.SQLQueryInfo;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.*;
import org.hibernate.Query;
import org.hibernate.hql.internal.ast.QueryTranslatorImpl;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.transform.Transformers;
import org.joda.time.DateTime;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dna.persistence.shiro.UserRoleToken.CID_BASE;

/**
 * Created by Aliang on 2017/4/27.
 */
public abstract class AbstractDAOMethod<T> extends HibernateDaoSupport {
    private Class<T> entity;
    private String keyField;
    private String tableName;
    private LinkedHashSet<Field> columnFields;
    private LinkedHashSet<String> columnList;

    private String cid;
    protected HibernateTemplate hibernateTemplate;
    private Map<String, SQLQueryInfo> countQueryInfoMap = new ConcurrentHashMap<>();
    private ThreadLocal<String> currentCountName = new ThreadLocal<>();

    private AbstractDAOMethod() {
    }

    public AbstractDAOMethod(String cid) {
        super.setSessionFactory(SessionFactoryHolder.get(cid));
        this.hibernateTemplate = super.getHibernateTemplate();
        this.cid = StringUtils.isBlank(cid) ? CID_BASE : cid;
        columnList = new LinkedHashSet<>();
        columnFields = new LinkedHashSet<>();
    }

    protected void setEntity(Class<T> entity) {
        this.entity = entity;
        this.tableName = entity.getAnnotation(Table.class).name();
        for (Field field : entity.getDeclaredFields()) {
            if (StringUtils.isBlank(keyField) && field.isAnnotationPresent(Id.class)) {
                keyField = field.getName();
            }
            if (field.isAnnotationPresent(Column.class)) {
                field.setAccessible(true);
                this.columnFields.add(field);
                this.columnList.add(field.getName());
            }
        }
        try {
            for (Method method : entity.getDeclaredMethods()) {
                if (StringUtils.isBlank(keyField) && method.isAnnotationPresent(Id.class)) {
                    keyField = getFieldNameByGetter(method);
                }
                if (method.isAnnotationPresent(Column.class)) {
                    String fieldName = getFieldNameByGetter(method);
                    Field field = entity.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    this.columnFields.add(field);
                    this.columnList.add(fieldName);
                }
            }
        } catch (NoSuchFieldException e) {
            logger.error("create DAO failed by entity[" + entity.getName() + "]", e);
            throw new UnknownEntityTypeException("can not get column info by entity[" + entity.getName() + "]");
        }
        if (StringUtils.isBlank(tableName) || StringUtils.isBlank(keyField) || columnFields.isEmpty() || columnList.isEmpty()) {
            throw new UnknownEntityTypeException("can not get column info by entity[" + entity.getName() + "]");
        }
    }

    @NotNull(message = "信息不存在")
    @DAOMethod
    public T get(Serializable id) {
        return hibernateTemplate.get(entity, id);
    }

    @NotNull(message = "保存失败")
    @DAOMethod
    public <B> B save(T v) {
        return (B) hibernateTemplate.save(v);
    }

    public void update(T v) {
        hibernateTemplate.update(v);
        hibernateTemplate.flush();
    }

    public void delete(T v) {
        hibernateTemplate.delete(v);
        hibernateTemplate.flush();
    }

    /**
     * 用于记录替换时使用
     */
    public void flush() {
        hibernateTemplate.flush();
    }

    public void executeInsertProcedure(Collection<T> v) {
        if (v.isEmpty()) {
            return;
        }
        hibernateTemplate.execute(session -> {
            try {
                Query query = session.createSQLQuery("{call pro_insert(:tableName,:columns,:records)}");
                List<String> valueList = new ArrayList<>();
                for (T e : v) {
                    List<String> values = new ArrayList<>();
                    for (Field f : columnFields) {
                        values.add(transferSQLParameter(f.get(e)));
                    }
                    String valueString = "(".concat(String.join(",", values)).concat(")");
                    if (!values.isEmpty()) {
                        valueList.add(valueString);
                    }
                }
                String columns = String.join(",", columnList);
                String records = String.join(",", valueList);
                query.setParameter("tableName", tableName);
                query.setParameter("columns", columns);
                query.setParameter("records", records);
                query.executeUpdate();
            } catch (IllegalAccessException e) {
                logger.error("mapping insert value failed by entity[" + entity.getName() + "]", e);
                throw new QueryException("can not executeInsert cause:" + e.getMessage());
            }
            return true;
        });
    }

    public void executeUpdateProcedure(Map<String, Object> params, Collection<?> keys) {
        if (params.isEmpty() || keys.isEmpty()) {
            return;
        }
        boolean result = hibernateTemplate.execute(session -> {
            Query query = session.createSQLQuery("{call pro_update(:tableName,:params,:primaryKey,:recordIds)}");
            List<String> updateParamList = new ArrayList<>();
            List<String> recordIdList = new ArrayList<>();
            for (String key : params.keySet()) {
                if (columnList.contains(key)) {
                    updateParamList.add(key.concat("=").concat(transferSQLParameter(params.get(key))));
                }
            }
            for (Object k : keys) {
                if (k != null) {
                    recordIdList.add(transferSQLParameter(k));
                }
            }
            String updateParams = String.join(",", updateParamList);
            String recordIds = String.join(",", recordIdList);
            query.setParameter("tableName", tableName);
            query.setParameter("params", updateParams);
            query.setParameter("primaryKey", keyField);
            query.setParameter("recordIds", recordIds);
            query.executeUpdate();
            return true;
        });
    }

    public void executeResetProcedure() {
        columnFields.forEach(field -> {
            if (field.getName().equals(keyField) && field.getAnnotation(GeneratedValue.class).strategy() != GenerationType.IDENTITY) {
                throw new QueryException("entity[" + entity.getSimpleName() + "] don't have auto_increment column to reset");
            }
        });
        hibernateTemplate.execute(session -> {
            Query query = session.createSQLQuery("{call pro_reset(:tableName,:primaryKey)}");
            query.setParameter("tableName", tableName);
            query.setParameter("primaryKey", keyField);
            query.executeUpdate();
            return true;
        });
    }

    public void updateBatch(List<T> v) {
        v.forEach(e -> hibernateTemplate.update(e));
        hibernateTemplate.flush();
        hibernateTemplate.clear();
    }

    public void deleteBatch(List<T> v) {
        hibernateTemplate.deleteAll(v);
        hibernateTemplate.flush();
        hibernateTemplate.clear();
    }

    public void saveBatch(List<T> v) {
        v.forEach(e -> {
            if (hibernateTemplate.save(e) == null) {
                throw new HibernateException(entity.getSimpleName() + "[" + JSON.toJSONString(e) + "] save error");
            }
        });
        hibernateTemplate.flush();
        hibernateTemplate.clear();
    }

    public String getCurrentCountName() {
        return currentCountName.get();
    }

    public void setCurrentCountName(String currentCountName) {
        this.currentCountName.set(currentCountName);
    }

    public Long getCount(final String countName, final Object... args) {
        final SQLQueryInfo queryInfo = countQueryInfoMap.get(generateCountName(countName, args));
        if (queryInfo == null) {
            throw new QueryException("count queryInfo not set:" + countName);
        }
        Long count = this.hibernateTemplate.execute(session -> {
            Query query;
            if (queryInfo.getQueryType() == QueryType.HQL) {
                query = session.createQuery(queryInfo.getQueryString());
                setParameterMap(query, queryInfo.getParameters());
                return (Long) query.uniqueResult();
            } else if (queryInfo.getQueryType() == QueryType.SQL) {
                query = session.createSQLQuery(queryInfo.getQueryString());
                if (queryInfo.getQueryString().contains(":")) {
                    setParameterMap(query, queryInfo.getParameters());
                } else if (queryInfo.getQueryString().contains("?")) {
                    int i = 0;
                    for (Object value : queryInfo.getParameters().values()) {
                        if (value instanceof Collection || value instanceof Object[]) {
                            query.setParameter(i++, JSONObject.toJSONString(value).replaceAll("[|]", ""));
                        } else {
                            query.setParameter(i++, value);
                        }
                    }
                }
                return ((BigInteger) query.uniqueResult()).longValue();
            } else {
                throw new QueryException("unknown queryType:" + queryInfo.getQueryType());
            }
        });
        return count;
    }

    public void setQueryInfo(final String countKey, SQLQueryInfo queryInfo) {
        countQueryInfoMap.put(countKey, queryInfo);
    }

    public boolean hasCountName(String countKey) {
        return countQueryInfoMap.containsKey(countKey);
    }

    public static String generateCountName(final String countName, final Object... args) {
        String argsCode = "(" + JSON.toJSONString(args) + ")";
        return HashEncryptUtils.MD5(countName.concat(argsCode));
    }

    protected <B> List<B> executeHQLForPage(final StringBuffer hql, final int start, final int limit, final Map<String, Object> params) {

        List<B> list = this.hibernateTemplate.execute(new HibernateCallback<List<B>>() {

            @Override
            public List<B> doInHibernate(Session session) throws HibernateException {
                List<B> list;
                Query query = session.createQuery(hql.toString());
                setParameterMap(query, params);
                query.setFirstResult(start);
                if (limit != 0) {
                    query.setMaxResults(limit);
                }
                queryCountHandler(hql.toString(), params, QueryType.HQL);
                list = query.list();
                return list;
            }
        });
        return list;
    }

    protected <B> List<B> executeHQL(final StringBuffer hql, final Map<String, Object> params) {

        List<B> list = this.hibernateTemplate.execute(new HibernateCallback<List<B>>() {

            @Override
            public List<B> doInHibernate(Session session) throws HibernateException {
                List<B> list;
                Query query = session.createQuery(hql.toString());
                setParameterMap(query, params);
                list = query.list();
                return list;
            }
        });
        return list;
    }

    protected Integer executeUpdate(final StringBuffer sql, final Map<String, Object> params, QueryType queryType) {

        Integer result = this.hibernateTemplate.execute(new HibernateCallback<Integer>() {
            @Override
            public Integer doInHibernate(Session session) throws HibernateException {
                Query query;
                if (queryType == QueryType.SQL) {
                    query = session.createSQLQuery(sql.toString());
                } else {
                    query = session.createQuery(sql.toString());
                }
                setParameterMap(query, params);
                return query.executeUpdate();
            }
        });

        return result;
    }

    protected List<Map<String, Object>> executeSQL(final StringBuffer sql, final Map<String, Object> params) {

        List<Map<String, Object>> result = this.hibernateTemplate.execute(new HibernateCallback<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> doInHibernate(Session session) throws HibernateException {
                SQLQuery query = session.createSQLQuery(sql.toString());
                query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
                setParameterMap(query, params);
                return query.list();
            }
        });

        return result;
    }

    protected List<Map<String, Object>> executeSQLForPage(final StringBuffer sql, final int start, final int limit, final Map<String, Object> params) {

        List<Map<String, Object>> result = this.hibernateTemplate.execute(new HibernateCallback<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> doInHibernate(Session session) throws HibernateException {
                SQLQuery query = session.createSQLQuery(sql.toString());
                query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
                query.setFirstResult(start);
                if (limit != 0) {
                    query.setMaxResults(limit);
                }
                setParameterMap(query, params);
                queryCountHandler(sql.toString(), params, QueryType.SQL);
                return query.list();
            }
        });

        return result;
    }

    private void queryCountHandler(String hql, Map<String, Object> params, QueryType queryType) {
        if (StringUtils.isNotBlank(getCurrentCountName()) && !countQueryInfoMap.containsKey(getCurrentCountName())) {
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            boolean result = false;
            try {
                for (int i = 3; i < elements.length; i++) {
                    if (elements[i].getLineNumber() > 0) {//非代理类
                        Class eClazz = Class.forName(elements[i].getClassName());
                        if (eClazz.getSuperclass() == AbstractDAOMethod.class) {
                            for (Method m : eClazz.getMethods()) {
                                if (m.getName().equals(elements[i].getMethodName())) {
                                    result = true;
                                    if (m.isAnnotationPresent(DAOCount.class)) {
                                        SQLQueryInfo queryInfo = generateQueryInfo(hql, params, queryType, m, getSessionFactory());
                                        countQueryInfoMap.put(getCurrentCountName(), queryInfo);
                                        break;
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                        if (result) {
                            break;
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                logger.error("", e);
                throw new QueryException("can not found DAOCount method for handler");
            }
        }
    }

    public static SQLQueryInfo generateQueryInfo(String hql, Map<String, Object> params, QueryType queryType, Method method, SessionFactory sessionFactory) {
        Pattern select = Pattern.compile("^\\s*(select).*", Pattern.CASE_INSENSITIVE);
        Pattern from = Pattern.compile("^\\s*(from).*", Pattern.CASE_INSENSITIVE);
        Pattern groupByOrDistinct = Pattern.compile(".*((group\\s+by)|(distinct)).*", Pattern.CASE_INSENSITIVE);
        Pattern pParam = Pattern.compile(":\\w*");

        StringBuffer queryString = new StringBuffer(hql);
        Map<String, Object> linkedParams = params;
        QueryType qt = queryType;
        if (queryType == QueryType.HQL) {
            if (groupByOrDistinct.matcher(hql).find()) {
                QueryTranslator queryTranslator = new QueryTranslatorImpl(hql, hql, Collections.EMPTY_MAP, (SessionFactoryImpl) sessionFactory);
                queryTranslator.compile(Collections.EMPTY_MAP, false);
                queryString = new StringBuffer(queryTranslator.getSQLString()).insert(0, "select count(*) from (").append(") as tmp");
                Matcher matcher = pParam.matcher(hql);
                linkedParams = new LinkedHashMap<>();
                while (matcher.find()) {
                    linkedParams.put(matcher.group().substring(1), params.get(matcher.group().substring(1)));
                }
                qt = QueryType.SQL;
            } else {
                if (select.matcher(hql).find()) {
                    queryString = new StringBuffer(hql.replaceFirst("^\\s*(select).*?(from)", "select count(*) from"));
                } else if (from.matcher(hql).find()) {
                    queryString.insert(0, "select count(*) ");
                } else {
                    throw new QueryException("can not resolve queryString to count in method[" + method.getDeclaringClass().getSimpleName() + "." + method.getName() + "] by queryCountHandler");
                }
            }
        } else if (queryType == QueryType.SQL) {
            queryString.insert(0, "select count(*) from (").append(") as tmp");
        } else {
            throw new QueryException("can not resolve queryType to count in method[" + method.getDeclaringClass().getSimpleName() + "." + method.getName() + "] by queryCountHandler");
        }
        SQLQueryInfo queryInfo = new SQLQueryInfo(queryString.toString(), linkedParams, qt);
        return queryInfo;
    }

    public static Query setParameterMap(Query query, Map<String, Object> params) {
        for (String key : params.keySet()) {
            if (params.get(key) instanceof Collection) {
                query.setParameterList(key, (List) params.get(key));
            } else if (params.get(key) instanceof Object[]) {
                query.setParameterList(key, (Object[]) params.get(key));
            } else {
                query.setParameter(key, params.get(key));
            }
        }
        return query;
    }

    private static String transferSQLParameter(Object parameter) {
        if (parameter == null) {
            return "null";
        }
        if (parameter instanceof Number) {
            return String.valueOf(parameter);
        } else if (parameter instanceof Boolean) {
            return (Boolean) parameter ? "1" : "0";
        } else if (parameter instanceof CharSequence) {
            return "'".concat(parameter.toString()).concat("'");
        } else if (parameter instanceof Date) {
            return "'".concat(new DateTime(parameter).toLocalDateTime().toString(DateConversion.DEFAULT_DATE_TIME)).concat("'");
        } else {
            throw new UnknownEntityTypeException("can not transfer parameter[" + parameter.toString() + "] to SQL type");
        }
    }

    private static String getFieldNameByGetter(Method method) {
        String fieldName = method.getName().replaceFirst("get", "");
        fieldName = Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1);
        return fieldName;
    }

    public String getCid() {
        return cid;
    }

    public Class<T> getEntity() {
        return entity;
    }

    public String getTableName() {
        return tableName;
    }
}
