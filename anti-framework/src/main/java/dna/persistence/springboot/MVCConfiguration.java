package dna.persistence.springboot;

import com.alibaba.fastjson.serializer.BeforeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter4;
import dna.origins.annotation.DictionaryItem;
import dna.origins.commons.SerializableEntity;
import dna.persistence.regulate.DictionaryService;
import dna.persistence.template.DataDictionary;
import dna.persistence.template.EnvConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

@Configuration
public class MVCConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MVCConfiguration.class);
    @Autowired
    DictionaryService dictionaryService;

    @Bean
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        //1.需要定义一个convert转换消息的对象;
        FastJsonHttpMessageConverter4 fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter4();
        //2:添加fastJson的配置信息;
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        if (EnvConfig.isDebug()) {
            fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat);//格式化输出
        }
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteNullBooleanAsFalse,
                SerializerFeature.WriteNullNumberAsZero);
        BeforeFilter beforeFilter = new BeforeFilter() {
            Pattern p1 = Pattern.compile("00$");
            Pattern p2 = Pattern.compile("0000$");
            @Override
            public void writeBefore(Object o) {
                if (o instanceof SerializableEntity) {
                    String cid = ((SerializableEntity) o).getCid();
                    Field[] fields = o.getClass().getDeclaredFields();
                    String writeKey = "";
                    String path = "";
                    try {
                        for (Field f : fields) {
                            if (f.isAnnotationPresent(DictionaryItem.class)) {
                                f.setAccessible(true);
                                writeKey = f.getName().endsWith("Id") || f.getName().endsWith("Code") ? f.getName().replaceFirst("Id$|Code$", "Name") : f.getName().concat("Text");
                                path = f.getAnnotation(DictionaryItem.class).value();
                                if (dictionaryService.getDicType(path) == null) {
                                    throw new NoSuchElementException("dictionary[" + path + "] not found");
                                }
                                if (dictionaryService.getDicType(path) == 1) {
                                    if (StringUtils.isBlank(cid)) {
                                        writeKeyValue(writeKey, "Unknown");
                                        continue;
                                    } else {
                                        path = path.concat(DictionaryService.separator.concat(cid));
                                    }
                                }
                                DataDictionary dataDictionary = dictionaryService.getDataDictionary(path);
                                if (dataDictionary == null) {
                                    throw new NoSuchElementException("dictionary[" + path + "] not found");
                                }
                                String key = String.valueOf(f.get(o));
                                String value = dataDictionary.get(key) == null ? "" : dataDictionary.get(key).getValue();
                                if (path.contains("Address.dic")) {
                                    if (StringUtils.isNotBlank(value)) {
                                        if (!p1.matcher(key).find()) {
                                            value = dataDictionary.get(key.replaceFirst("..$", "00")).getValue().concat(value);
                                            value = dataDictionary.get(key.replaceFirst("....$", "0000")).getValue().concat(value);
                                        } else if (!p2.matcher(key).find()) {
                                            value = dataDictionary.get(key.replaceFirst("....$", "0000")).getValue().concat(value);
                                        }
                                    }
                                }
                                writeKeyValue(writeKey, value);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("MessageConverters error:Mapping dictionary[{}] failed for entity[{}] by key[{}]:", path, o.getClass(), writeKey, e);
                    }
                }
            }
        };
        fastJsonConfig.setSerializeFilters(beforeFilter);
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        //3处理中文乱码问题
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        //4.在convert中添加配置信息.
        fastJsonHttpMessageConverter.setSupportedMediaTypes(fastMediaTypes);
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        //处理text/plain乱码
        StringHttpMessageConverter stringHttpMessageConverters = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.TEXT_PLAIN);
        stringHttpMessageConverters.setSupportedMediaTypes(mediaTypes);

        converters.add(fastJsonHttpMessageConverter);
        converters.add(stringHttpMessageConverters);
        return new HttpMessageConverters(converters);
    }
}
