package dna.persistence.factory;

import dna.origins.annotation.RpcService;
import dna.persistence.spring.AopTargetUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Aliang on 2017/7/23.
 */
@Component("appContextHolder")
public class AppContextHolder implements ApplicationContextAware {
    private static ApplicationContext applicationContext;     //Spring应用上下文环境
    public static final String CLASSPATH_ONLY_PREFIX = "classpath:";
    public static final String CLASSPATH_ALL_PREFIX = "classpath*:";

    /**
     * 实现ApplicationContextAware接口的回调方法，设置上下文环境
     *
     * @param applicationContext
     * @throws BeansException
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AppContextHolder.applicationContext = applicationContext;
    }

    /**
     * 获取对象
     *
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     */
    public static Object getBean(String name) throws BeansException {
        return applicationContext.getBean(name);
    }

    /**
     * 获取对象
     *
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     */
    public static List<Method> getRpcMethods(String name) throws BeansException {
        Class clazz = applicationContext.getType(name);
        Method[] dms = clazz.getDeclaredMethods();
        Method[] dds = clazz.getMethods();
        if (clazz.isAnnotationPresent(RpcService.class)) {
            return Arrays.asList(dms);
        } else {
            List<Method> ms = new ArrayList<>();
            for (Method m : dms) {
                if (m.isAnnotationPresent(RpcService.class)) {
                    ms.add(m);
                }
            }
            return ms;
        }
    }

    /**
     * 获取目标对象
     *
     * @param bean
     * @return Object 一个以所给名字注册的bean的实例
     * @throws BeansException
     */
    public static List<Method> getRpcMethods(Object bean, String mName) throws Exception {
        Class bClass = bean.getClass();
        Object target = AopTargetUtil.getTarget(bean);
        Class tClazz = target.getClass();
        boolean isProxy = !bClass.equals(tClazz);
        boolean isRpc = tClazz.isAnnotationPresent(RpcService.class);
        Method[] dms = tClazz.getMethods();
        List<Method> methods = new ArrayList<>();
        for (Method m : dms) {
            if (m.getName().equals(mName) && (isRpc || m.isAnnotationPresent(RpcService.class))) {
                methods.add(isProxy ? bClass.getDeclaredMethod(m.getName(), m.getParameterTypes()) : m);
            }
        }
        return methods;
    }

    /**
     * 获取目标对象方法的參數類型
     *
     * @param bean
     * @return 类型数组
     * @throws BeansException
     */
    public static Type[] getTargetMethodParameterTypes(Object bean, Method pMethod) throws Exception {
        Object target = AopTargetUtil.getTarget(bean);
        Class tClazz = target.getClass();
        Type[] gpt = tClazz.getMethod(pMethod.getName(), pMethod.getParameterTypes()).getGenericParameterTypes();
        return gpt;
    }

    /**
     * 获取类型为requiredType的对象
     * 如果bean不能被类型转换，相应的异常将会被抛出（BeanNotOfRequiredTypeException）
     *
     * @param name         bean注册名
     * @param requiredType 返回对象类型
     * @return Object 返回requiredType类型对象
     * @throws BeansException
     */
    public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(name, requiredType);
    }

    /**
     * 获取类型为requiredType的对象
     *
     * @param requiredType 返回对象类型
     * @return Map 返回requiredType类型对象集合
     * @throws BeansException
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> requiredType) throws BeansException {
        return applicationContext.getBeansOfType(requiredType);
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     *
     * @param name
     * @return boolean
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是一个prototype。
     * 如果与给定名字相应的bean定义没有被找到，将会抛出一个异常（NoSuchBeanDefinitionException）
     *
     * @param name
     * @return boolean
     * @throws NoSuchBeanDefinitionException
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.isSingleton(name);
    }

    /**
     * @param name
     * @return Class 注册对象的类型
     * @throws NoSuchBeanDefinitionException
     */
    public static Class getType(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name
     * @return
     * @throws NoSuchBeanDefinitionException
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getAliases(name);
    }

    public static Resource getResource(String path) throws IOException {
        return applicationContext.getResource(path);
    }

    public static Resource[] getResources(String path) throws IOException {
        return applicationContext.getResources(path);
    }
}