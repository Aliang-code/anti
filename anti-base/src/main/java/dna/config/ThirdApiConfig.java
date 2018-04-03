package dna.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:config.properties")
@Configuration
@ConfigurationProperties(prefix = "tai")
public class ThirdApiConfig {
    private String amap_ak;
    private String amap_district_url;

    public String getAmap_ak() {
        return amap_ak;
    }

    public void setAmap_ak(String amap_ak) {
        this.amap_ak = amap_ak;
    }

    public String getAmap_district_url() {
        return amap_district_url;
    }

    public void setAmap_district_url(String amap_district_url) {
        this.amap_district_url = amap_district_url;
    }
}
