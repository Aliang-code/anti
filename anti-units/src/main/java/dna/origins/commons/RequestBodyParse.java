package dna.origins.commons;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;

/**
 * Created by Aliang on 2017/7/26.
 */
public class RequestBodyParse {

    private static Logger logger = LoggerFactory.getLogger(RequestBodyParse.class);

    public static <T> T parseObject(String text, Type type) throws Exception {
        Class<?> clazz;
        if (type instanceof ParameterizedType) {
            ParameterizedType pzType = (ParameterizedType) type;
            clazz = pzType.getClass();
        } else {
            clazz = (Class<T>) type;
        }
        if (StringUtils.isEmpty(text)) {
            if (clazz.isPrimitive()) {
                throw new IllegalArgumentException("param[" + text + "] cannot cast to " + clazz.getName());
            } else {
                return null;
            }
        }
        if (clazz == String.class) {
            return (T) text;
        } else if (clazz == Character.class || clazz == char.class) {
            if (text.length() > 1) {
                throw new IllegalArgumentException("param[" + text + "] cannot cast to " + clazz.getName());
            } else {
                return (T) ((Object) text.charAt(0));
            }
        } else if (clazz == Date.class || clazz == java.sql.Date.class) {
            return (T) new DateTime(text).toLocalDateTime().toDate();
            //return (T) DateConversion.parseDate(text, DateConversion.DEFAULT_DATE_TIME);
        } else {
            return JSONObject.parseObject(text, type);
        }
    }

}
