package dna.persistence.springboot;

import dna.origins.util.toolbar.AliyunUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:config.properties")
@Configuration
public class ToolsConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "tools.aliyun")
    public AliyunUtils aliyunUtils() {
        return new AliyunUtils();
    }
}
