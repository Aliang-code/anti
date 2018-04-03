package dna.persistence.springboot;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter4;
import dna.origins.util.client.HttpClientUtils;
import org.apache.http.client.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public HttpClient httpClient() {
        return HttpClientUtils.DefaultHttpClient(200);
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory(httpClient());
    }

    @Bean
    public RestTemplate defaultRestTemplate() {
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory());
        restTemplate.getMessageConverters().removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
        FastJsonHttpMessageConverter4 fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter4();
        fastJsonHttpMessageConverter.setSupportedMediaTypes(new ArrayList<MediaType>() {{
            add(MediaType.APPLICATION_JSON);
            add(MediaType.APPLICATION_JSON_UTF8);
            add(MediaType.TEXT_PLAIN);
        }});
        restTemplate.getMessageConverters().add(fastJsonHttpMessageConverter);
        return restTemplate;
    }

}
