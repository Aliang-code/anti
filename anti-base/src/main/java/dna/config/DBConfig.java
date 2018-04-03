package dna.config;

import dna.origins.commons.DatabaseType;
import dna.persistence.factory.HibernateTemplateFactory;
import dna.persistence.hibernate.DigestDriverDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

@Configuration
@PropertySource("classpath:dataSource.properties")
public class DBConfig {


    @Bean(initMethod = "init", destroyMethod = "close")
    @ConfigurationProperties(prefix = "spring.datasource.base")
    public DigestDriverDataSource dataSourceBean_base() throws Exception {
        return DigestDriverDataSource.generateAtomikosDataSourceBean();
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    @ConfigurationProperties(prefix = "spring.datasource.0001")
    public DigestDriverDataSource dataSourceBean_0001() throws Exception {
        return DigestDriverDataSource.generateAtomikosDataSourceBean();
    }

    @Bean
    public LocalSessionFactoryBean sessionFactoryBean_base() throws Exception {
        return HibernateTemplateFactory.generateSessionFactory(DatabaseType.MYSQL, dataSourceBean_base());
    }

    @Bean
    public LocalSessionFactoryBean sessionFactoryBean_0001() throws Exception {
        return HibernateTemplateFactory.generateSessionFactory(DatabaseType.MYSQL, dataSourceBean_0001());
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    @ConfigurationProperties(prefix = "spring.datasource.1802")
    @ConditionalOnProperty(prefix = "spring.datasource.1802", name = "uniqueResourceName", havingValue = "1802")
    public DigestDriverDataSource dataSourceBean_1802() throws Exception {
        return DigestDriverDataSource.generateAtomikosDataSourceBean();
    }

    @Bean
    @ConditionalOnBean(name = "dataSourceBean_1802")
    public LocalSessionFactoryBean sessionFactoryBean_1802() throws Exception {
        return HibernateTemplateFactory.generateSessionFactory(DatabaseType.MYSQL, dataSourceBean_1802());
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    @ConfigurationProperties(prefix = "spring.datasource.4183")
    @ConditionalOnProperty(prefix = "spring.datasource.4183", name = "uniqueResourceName", havingValue = "4183")
    public DigestDriverDataSource dataSourceBean_4183() throws Exception {
        return DigestDriverDataSource.generateAtomikosDataSourceBean();
    }

    @Bean
    @ConditionalOnBean(name = "dataSourceBean_4183")
    public LocalSessionFactoryBean sessionFactoryBean_4183() throws Exception {
        return HibernateTemplateFactory.generateSessionFactory(DatabaseType.MYSQL, dataSourceBean_4183());
    }
}
