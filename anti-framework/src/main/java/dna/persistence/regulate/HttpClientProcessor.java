package dna.persistence.regulate;

import com.alibaba.fastjson.JSONObject;
import dna.origins.commons.ResObject;
import dna.origins.commons.ResultCode;
import dna.origins.commons.SignType;
import dna.origins.util.toolbar.HttpConversion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class HttpClientProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HttpClientProcessor.class);

    /**
     * 默认的restTemplate
     */
    @Autowired
    private RestTemplate defaultRestTemplate;

    public <T> ResObject<T> postJsonRequest(String url, JSONObject postData, ParameterizedTypeReference<T> type) {
        //创建请求体
        RequestEntity requestEntity = RequestEntity.post(URI.create(url)).contentType(MediaType.APPLICATION_JSON_UTF8).body(postData);
        return responseHandler(defaultRestTemplate, requestEntity, type);
    }

    public <T> ResObject<T> getSimpleRequest(String url, ParameterizedTypeReference<T> type) {
        //创建请求体
        RequestEntity requestEntity = RequestEntity.get(URI.create(url)).build();
        return responseHandler(defaultRestTemplate, requestEntity, type);
    }

    /**
     * 使用默认连接池创建签名请求
     *
     * @param url
     * @param postData
     * @param appSecret
     * @param signType
     * @param type
     * @param <T>
     * @return
     */
    public <T> ResObject<T> postJsonRequestWithSign(String url, JSONObject postData, String appSecret, SignType signType, ParameterizedTypeReference<T> type) {
        return postJsonRequestWithSign(defaultRestTemplate, url, postData, appSecret, signType, type);
    }

    public <T> ResObject<T> postJsonRequestWithSign(RestTemplate restTemplate, String url, JSONObject postData, String appSecret, SignType signType, ParameterizedTypeReference<T> type) {
        String sign = HttpConversion.generateSign(appSecret, postData, signType);
        postData.put(HttpConversion.PARAM_SIGN, sign);
        //创建请求体
        RequestEntity requestEntity = RequestEntity.post(URI.create(url)).contentType(MediaType.APPLICATION_JSON_UTF8).body(postData);
        return responseHandler(restTemplate, requestEntity, type);
    }

    public <T> ResObject<T> responseHandler(RestTemplate restTemplate, RequestEntity requestEntity, ParameterizedTypeReference<T> type) {
        ResObject<T> resObject = null;
        try {
            //执行请求
            ResponseEntity<T> responseEntity = restTemplate.exchange(requestEntity, type);
            resObject = new ResObject<>(responseEntity.getStatusCode(), responseEntity.getBody());
            return resObject;
        } catch (Exception e) {
            if (e instanceof HttpStatusCodeException) {
                HttpStatusCodeException ex = (HttpStatusCodeException) e;
                resObject = new ResObject<>(ex.getStatusCode().value(), ex.getResponseBodyAsString());
            } else {
            }
            if (resObject == null || resObject.getCode() == null) {
                resObject = new ResObject<>(ResultCode.SERVER_ERROR);
            }
            logger.error("can not resolve request to url[{}]\n msg:{},cause:", requestEntity.getUrl().toString(), resObject.getMsg(), e);
            return resObject;
        }
    }
}
