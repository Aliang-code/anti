package dna.persistence.springboot;

import dna.persistence.factory.FTPClientFactory;
import dna.persistence.regulate.FTPClientProcessor;
import dna.persistence.template.FTPClientConfig;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:config.properties")
@ConditionalOnClass({GenericObjectPool.class, FTPClient.class})
@ConditionalOnProperty(value = "spring.ftp.enable", havingValue = "true")
public class FTPConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "ftp")
    public FTPClientConfig ftpClientConfig() {
        return new FTPClientConfig();
    }

    @Bean
    public FTPClientFactory ftpClientFactory(FTPClientConfig ftpClientConfig) {
        return new FTPClientFactory(ftpClientConfig);
    }

    @Bean
    @ConfigurationProperties(prefix = "ftp.pool")
    public GenericObjectPoolConfig genericObjectPoolConfig() {
        return new GenericObjectPoolConfig();
    }

    @Bean(initMethod = "init", destroyMethod = "close")
    public FTPClientProcessor ftpClientProcessor(FTPClientFactory factory, GenericObjectPoolConfig poolConfig) {
        // 其他未设置属性使用默认值，可根据需要添加相关配置
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        //处理空闲对象设置
        poolConfig.setSoftMinEvictableIdleTimeMillis(60000);//空闲多久后逐出
        poolConfig.setTimeBetweenEvictionRunsMillis(30000);//多久检查一次空闲
        return new FTPClientProcessor(factory, poolConfig);
    }

}
