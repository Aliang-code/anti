package dna.persistence.factory;

import dna.persistence.template.Context;

import java.util.Map;

/**
 * Created by Aliang on 2017/8/12.
 */
public class ContextUtil {
    private static ThreadLocal<Context> localContext = new ThreadLocal();

    private static void setContext(Context ctx) {
        localContext.set(ctx);
    }

    private static Context getContext() {
        Context ctx = localContext.get();
        if (ctx == null) {
            ctx = new Context();
            setContext(ctx);
        }
        return ctx;
    }

    public static <T> T get(String key, Class<T> clazz) {
        return (T) get(key);
    }

    public static Object get(String key) {
        return getContext().get(key);
    }

    public static void put(String k, Object v) {
        getContext().put(k, v);
    }

    public static void putAll(Map<String, Object> m) {
        getContext().putAll(m);
    }

    public static void remove(String key) {
        getContext().remove(key);
    }

    public static void clear() {
        localContext.remove();
    }

    public static Boolean has(String key) {
        Context ctx = localContext.get();
        return ctx == null ? false : ctx.containsKey(key);
    }
}
