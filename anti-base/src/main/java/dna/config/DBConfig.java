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
    @ConditionalOnProperty(prefix = "spring.datasource.0001", name = "uniqueResourceName", havingValue = "0001")
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

}
