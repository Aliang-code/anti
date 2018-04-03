package dna.origins.commons;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Aliang on 2017/8/10.
 */
public class EhcacheUtils {
    private static final String path = "/ehcache.xml";
    private URL url;

    private CacheManager manager;

    private static EhcacheUtils ehCache;

    private EhcacheUtils(String path) {
        url = getClass().getResource(path);
        manager = CacheManager.create(url);
    }

    public static EhcacheUtils instance() {
        if (ehCache == null) {
            ehCache = new EhcacheUtils(path);
        }
        return ehCache;
    }


    public void put(String cacheName, String key, Object value) {
        Cache cache = manager.getCache(cacheName);
        Element element = new Element(key, value);
        cache.put(element);
        //cache.flush();//很重要！防止被GC回收
    }

    public Object get(String cacheName, String key) {
        Cache cache = manager.getCache(cacheName);
        Element element = cache.get(key);
        return element == null ? null : element.getObjectValue();
    }

    public List<Object> getAll(String cacheName) {
        Cache cache = manager.getCache(cacheName);
        Map<Object, Element> map = cache.getAll(cache.getKeys());
        List<Object> list = new ArrayList<>();
        if (map != null) {
            map.values().forEach(element -> list.add(element.getObjectValue()));
        }
        return list;
    }

    public Cache get(String cacheName) {
        return manager.getCache(cacheName);
    }


    public void remove(String cacheName, String key) {
        Cache cache = manager.getCache(cacheName);
        cache.remove(key);
    }

    public void clear() {
        manager.clearAll();
    }

    public void shutdown() {
        manager.shutdown();
    }
}
