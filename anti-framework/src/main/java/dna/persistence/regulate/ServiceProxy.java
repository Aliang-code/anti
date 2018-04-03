package dna.persistence.regulate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dna.origins.commons.RequestBodyParse;
import dna.origins.commons.ResObject;
import dna.origins.commons.ResultCode;
import dna.origins.util.constants.JWTConstant;
import dna.persistence.factory.AppContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;


/**
 * Created by Aliang on 2017/7/20.
 * anti-web rpc访问反射
 */
@CrossOrigin(origins = "*", exposedHeaders = {JWTConstant.TOKEN_HEAD_NAME})
@Controller
public class ServiceProxy {
    private static Logger logger = LoggerFactory.getLogger(ServiceProxy.class);

    @PostMapping(value = "/*.jsonRequest")
    public ResponseEntity jsonRequest(HttpServletRequest request) throws Exception {
        BufferedReader br = request.getReader();
        String tmp;
        StringBuilder reqContent = new StringBuilder("");
        while ((tmp = br.readLine()) != null) {
            reqContent.append(tmp);
        }
        logger.debug(reqContent.toString());
        JSONObject jsonData = JSONObject.parseObject(reqContent.toString());
        String beanName = jsonData.getString("serviceId");
        String methodName = jsonData.getString("method");
        String body = jsonData.getString("body");
        logger.debug("body:{}", body);
        List<String> params = JSON.parseArray(body, String.class);
        Object bean = AppContextHolder.getBean(beanName);
        List<Method> methods = AppContextHolder.getRpcMethods(bean, methodName);
        Method noParamMethod = null;
        Boolean exactMethod = false;
        Object result = null;
        for (Method m : methods) {
            m.setAccessible(true);
            Type[] gpt = AppContextHolder.getTargetMethodParameterTypes(bean, m);
            //Class<?>[] pc = m.getParameterTypes();
            Object[] ops = new Object[gpt.length];
            if (gpt.length == 0) {
                noParamMethod = m;
            } else if (gpt.length == params.size()) {
                exactMethod = true;
                for (int i = 0; i < gpt.length; i++) {
                    ops[i] = RequestBodyParse.parseObject(params.get(i), gpt[i]);
                }
            } else {
                continue;
            }
            if (exactMethod) {
                if (m.getReturnType() != void.class) {
                    result = m.invoke(bean, ops);
                } else {
                    m.invoke(bean, ops);
                }
                break;
            }
        }
        if (!exactMethod) {
            if (noParamMethod != null) {
                if (noParamMethod.getReturnType() != void.class) {
                    result = noParamMethod.invoke(bean);
                } else {
                    noParamMethod.invoke(bean);
                }
            } else {
                throw new NoSuchMethodException("service[" + beanName + "] method[" + methodName + "] not found");
            }
        }
        logger.debug(JSON.toJSONString(result));
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject<>(ResultCode.REQUESST_SUCCESS, result));
    }

//    @RequestMapping(value = "/health")
//    public ResponseEntity health() throws Exception {
//        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject(ResultCode.REQUESST_SUCCESS));
//    }
}
