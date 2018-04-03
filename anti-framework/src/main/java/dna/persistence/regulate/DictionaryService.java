package dna.persistence.regulate;

import dna.origins.commons.SerializableEntity;
import dna.origins.util.constants.DictionaryConstant;
import dna.persistence.factory.AppContextHolder;
import dna.persistence.factory.HibernateTemplateFactory;
import dna.persistence.hibernate.SessionFactoryHolder;
import dna.persistence.template.DataDictionary;
import dna.persistence.template.Item;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.transaction.NotSupportedException;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@DependsOn({"sessionFactoryHolder"})
public class DictionaryService {
    private static final Logger logger = LoggerFactory.getLogger(DictionaryService.class);
    private static final ConcurrentHashMap<String, DataDictionary> dicMap = new ConcurrentHashMap<>();
    private static final String ROOT_PATH = "dna";
    private static final Map<Class, Set<String>> entityKeyMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> dicTypeMap = new ConcurrentHashMap<>();
    private static final Map<String, Lock> keyLockMap = new ConcurrentHashMap<>();
    public static final int DIC_TYPE_COMMON = 0;
    public static final int DIC_TYPE_SPECIAL = 1;
    public static final String separator = "_";


    /**
     * 获取整个字典
     *
     * @param path
     * @return
     */
    public DataDictionary getDataDictionary(String path) {
        return dicMap.get(path);
    }

    /**
     * 获取字典某个值
     *
     * @param path
     * @return
     */
    public static String getDictionaryValue(String path, Object key) {
        return dicMap.get(path).get(String.valueOf(key)).getValue();
    }

    @PostConstruct
    public void init() {
        try {
            Resource[] resources = AppContextHolder.getResources(AppContextHolder.CLASSPATH_ALL_PREFIX + "dna/**/*.dic");
            Set<String> cidSet = SessionFactoryHolder.getCidSet();
            for (Resource r : resources) {
                String key = r.getURL().getPath().replaceFirst("(.*/|\\\\)" + ROOT_PATH + "(/|\\\\)", "");
                loadDictionary(key.intern(), r.getInputStream(), cidSet);
                logger.info("dictionary[{}] loading complete", key);
            }
        } catch (Exception e) {
            throw new BeanInitializationException("dictionary loading error:", e);
        }
    }

    /**
     * 根据文件系统查找字典文件，弃用：jar包中无法使用
     *
     * @param path
     * @return
     */
    @Deprecated
    private List<File> findDicFiles(String path) {
        logger.info(path);
        File rootDir = new File(path);
        File[] files = rootDir.listFiles();
        List<File> dicList = new ArrayList<>();
        for (File f : files) {
            if (f.isDirectory()) {
                dicList.addAll(findDicFiles(f.getPath()));
            } else if (f.getName().endsWith(".dic")) {
                dicList.add(f);
            }
        }
        return dicList;
    }

    public void loadDictionary(String key, InputStream in, Set<String> cidSet) throws Exception {
        SAXReader reader = new SAXReader();
        final Document document = reader.read(in);
        Element root = document.getRootElement();
        if (root.attributeValue(DictionaryConstant.ROOT_CLASS) == null) {
            LinkedHashMap<String, Item> itemMap = new LinkedHashMap<>();
            for (Element element : (List<Element>) root.elements()) {
                if (DictionaryConstant.ITEM_NAME.equals(element.getName())) {
                    String itemKey = element.attributeValue(DictionaryConstant.PARAM_KEY);
                    if (itemKey == null) {
                        throw new NullArgumentException(DictionaryConstant.PARAM_KEY);
                    }
                    Item item = new Item(element.attributeValue(DictionaryConstant.PARAM_PARENT), itemKey, element.getText());
                    itemMap.put(itemKey, item);
                    if (StringUtils.isNotEmpty(item.getParent())) {
                        Item parent;
                        if ((parent = itemMap.get(item.getParent())) == null) {
                            throw new NoSuchElementException("dictionary[" + key + "] loading failed: missing parent for item[" + item.getKey() + "]");
                        }
                        parent.addChild(item);
                    }
                } else {
                    logger.warn("dictionary[{}] has nonstandard element:{}", key, element.getName());
                }
            }
            dicMap.put(key, new DataDictionary(itemMap));
            dicTypeMap.put(key, DIC_TYPE_COMMON);
        } else {
            Class clazz = Class.forName(root.attributeValue(DictionaryConstant.ROOT_CLASS));
            String spec = root.attributeValue(DictionaryConstant.ROOT_SPEC);
            String field = root.attributeValue(DictionaryConstant.ROOT_FIELD);
            if (StringUtils.isBlank(spec)) {
                throw new NullArgumentException(DictionaryConstant.ROOT_SPEC);
            }
            if (!clazz.isAnnotationPresent(Entity.class)) {
                throw new NotSupportedException("dictionary[" + key + "] class[" + clazz.getName() + "] is not an entity class");
            }
            Field keyField = null;
            if (StringUtils.isBlank(field)) {
                for (Method m : clazz.getMethods()) {
                    if (m.isAnnotationPresent(Id.class)) {
                        keyField = clazz.getDeclaredField(Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4));
                    }
                }
                if (keyField == null) {
                    for (Field f : clazz.getDeclaredFields()) {
                        if (f.isAnnotationPresent(Id.class)) {
                            keyField = f;
                        }
                    }
                }
            } else {
                keyField = clazz.getDeclaredField(field);
            }
            if (keyField == null) {
                throw new NoSuchFieldException("dictionary[" + key + "] can not found Id field in class[" + clazz.getName() + "]");
            }
            if (keyField.getName().equals(spec) || clazz.getDeclaredField(spec) == null) {
                throw new IllegalArgumentException("dictionary[" + key + "] spec is invalid");
            }
            final String keyName = keyField.getName();
            if (clazz.getSuperclass() == SerializableEntity.class) {
                for (String cid : cidSet) {
                    LinkedHashMap<String, Item> itemMap = new LinkedHashMap<>();
                    List<Object[]> result = HibernateTemplateFactory.getTempByCid(cid).execute((session) -> {
                        String hql = "select ".concat(keyName).concat(",").concat(spec).concat(" from ".concat(clazz.getSimpleName()));
                        Query query = session.createQuery(hql.toString());
                        return query.list();
                    });
                    result.forEach((r) -> itemMap.put(String.valueOf(r[0]), new Item(String.valueOf(r[0]), String.valueOf(r[1]))));
                    dicMap.put(key.concat(separator.concat(cid)), new DataDictionary(itemMap));
                }
                dicTypeMap.put(key, DIC_TYPE_SPECIAL);
            } else {
                LinkedHashMap<String, Item> itemMap = new LinkedHashMap<>();
                List<Object[]> result = HibernateTemplateFactory.getTempByCid(null).execute((session) -> {
                    String hql = "select ".concat(keyName).concat(",").concat(spec).concat(" from ".concat(clazz.getSimpleName()));
                    Query query = session.createQuery(hql.toString());
                    return query.list();
                });
                result.forEach((r) -> itemMap.put(String.valueOf(r[0]), new Item(String.valueOf(r[0]), String.valueOf(r[1]))));
                dicMap.put(key, new DataDictionary(itemMap));
                dicTypeMap.put(key, DIC_TYPE_COMMON);
            }
            Set<String> keyList = entityKeyMap.getOrDefault(clazz, new HashSet<>());
            keyList.add(key);
            entityKeyMap.put(clazz, keyList);
        }
    }

    private Lock getKeyLock(String lockKey) {
        Lock lock = keyLockMap.get(lockKey);
        if (lock == null) {
            lock = new ReentrantLock();
            keyLockMap.put(lockKey, lock);
            return getKeyLock(lockKey);
        } else {
            return lock;
        }
    }

    public void updateDictionary(Set<String> keySet, String cid) {
        Runnable run = () -> {
            for (String key : keySet) {
                String lockKey = key.concat(cid == null ? "" : cid);
                Lock lock = getKeyLock(lockKey);
                if (lock.tryLock()) {//10秒内统一更新
                    try {
                        Thread.sleep(10000);
                        InputStream in = AppContextHolder.getResource(AppContextHolder.CLASSPATH_ONLY_PREFIX.concat(ROOT_PATH.concat("/".concat(key)))).getInputStream();
                        loadDictionary(key, in, new HashSet<String>() {{
                            add(cid);
                        }});
                        keyLockMap.remove(lockKey, lock);
                        logger.info("dictionary[{}] update complete", key.concat(cid == null ? "" : cid));
                    } catch (Exception e) {
                        throw new BeanInitializationException("dictionary update error:", e);
                    } finally {
                        lock.unlock();
                    }
                }
            }
        };
        Thread thread = new Thread(run);
        thread.start();
    }

    public Set<String> getEntityKeys(Class entityClass) {
        return entityKeyMap.get(entityClass);
    }

    public boolean hasDicEntity(Class entityClass) {
        return entityKeyMap.containsKey(entityClass);
    }

    public Integer getDicType(String key) {
        return dicTypeMap.get(key);
    }
}
