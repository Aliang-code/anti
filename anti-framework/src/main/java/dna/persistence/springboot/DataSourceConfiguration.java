package dna.persistence.springboot;

import dna.persistence.hibernate.SessionFactoryHolder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

import java.lang.reflect.Constructor;

@Configuration
@PropertySource(value = {"classpath:dataSource.properties", "classpath:config.properties"}, ignoreResourceNotFound = true)
public class DataSourceConfiguration {

    @Bean(name = "sessionFactoryHolder", initMethod = "init")
    @DependsOn({"appContextHolder"})
    @ConfigurationProperties(prefix = "dna.sessionFactory")
    public SessionFactoryHolder sessionFactoryHolder() throws Exception {
        Constructor<SessionFactoryHolder> c = SessionFactoryHolder.class.getDeclaredConstructor();
        c.setAccessible(true);
        SessionFactoryHolder sessionFactoryHolder = c.newInstance();
        return sessionFactoryHolder;
    }

}
