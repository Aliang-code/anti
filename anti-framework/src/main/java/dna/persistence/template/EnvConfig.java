package dna.persistence.template;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;

@PropertySource("classpath:config.properties")
@Configuration
@ConfigurationProperties(prefix = "env")
@Order(-1)
public class EnvConfig {
    public static String model;
    public static String serverUrl;
    public static Boolean showSql;

    public static boolean isDebug() {
        return !"prod".equalsIgnoreCase(model);
    }

    public static void setModel(String model) {
        EnvConfig.model = model;
    }

    public static void setServerUrl(String serverUrl) {
        EnvConfig.serverUrl = serverUrl;
    }

    public static void setShowSql(Boolean showSql) {
        EnvConfig.showSql = showSql;
    }

    public static String generateUrl(String downloadPath, Integer fileId) {
        return serverUrl.concat(downloadPath.concat("/" + String.valueOf(fileId)));
    }
}
