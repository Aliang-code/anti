package dna.auth.config;

import dna.persistence.shiro.ApplicationRealm;
import dna.persistence.shiro.StatelessDefaultSubjectFactory;
import dna.persistence.shiro.StatelessRealm;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.*;

@ConditionalOnProperty(name = "spring.shiro.enable",havingValue = "true")
@Configuration
public class ShiroConfiguration {

    @Bean
    public StatelessRealm statelessRealm() {
        StatelessRealm realm = new StatelessRealm();
        realm.setCachingEnabled(false);
        return realm;
    }

    @Bean
    public ApplicationRealm applicationRealm() {
        ApplicationRealm realm = new ApplicationRealm();
        realm.setCachingEnabled(false);
        return realm;
    }

    @Bean
    public StatelessDefaultSubjectFactory subjectFactory() {
        return new StatelessDefaultSubjectFactory();
    }

    @Bean
    public DefaultWebSessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setSessionValidationSchedulerEnabled(false);
        return sessionManager;
    }

    @Bean
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setSessionManager(sessionManager());
        securityManager.setSubjectFactory(subjectFactory());
        List<Realm> realms = new ArrayList<>();
        realms.add(statelessRealm());
        realms.add(applicationRealm());
        securityManager.setRealms(realms);
        ((DefaultSessionStorageEvaluator) ((DefaultSubjectDAO) securityManager.getSubjectDAO()).getSessionStorageEvaluator()).setSessionStorageEnabled(false);
        SecurityUtils.setSecurityManager(securityManager);
        return securityManager;
    }


    @Bean
    public ShiroFilterFactoryBean shirFilter() {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setSecurityManager(securityManager());
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("statelessAuth", new StatelessAuthFilter());
        shiroFilterFactoryBean.setFilters(filterMap);
        // 拦截器(按顺序覆盖后面的)
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("/*.jsonRequest", "statelessAuth");
        //mapping.put("/download/**", "statelessAuth");
        mapping.put("/auth/**", "statelessAuth");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(mapping);
        return shiroFilterFactoryBean;
    }

    /**
     * 开启shiro aop注解支持. 使用代理方式;所以需要开启代码支持;才能使用@RequiresPermissions
     */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager());
        return authorizationAttributeSourceAdvisor;
    }

//    防止加入filter链
//    @Bean
//    public StatelessAuthFilter statelessAuthFilter() {
//        return new StatelessAuthFilter();
//    }

//    @Bean
//    public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
//        return new LifecycleBeanPostProcessor();
//    }
//    @Bean
//    @DependsOn("lifecycleBeanPostProcessor")
//    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator(){
//        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator= new DefaultAdvisorAutoProxyCreator();
//        advisorAutoProxyCreator.setProxyTargetClass(true);
//        return advisorAutoProxyCreator;
//    }
}
