package dna.persistence.regulate;

import dna.origins.commons.ResObject;
import dna.origins.commons.ResultCode;
import dna.origins.commons.ServiceException;
import dna.persistence.template.EnvConfig;
import org.apache.shiro.ShiroException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;

@ControllerAdvice(annotations = {Controller.class, RestController.class})
public class ExceptionReaper {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionReaper.class);
    private static final String ERR_MSG = "errMsg";

    @ExceptionHandler({Exception.class})
    public Object serverException(Throwable ex, HttpServletRequest request, Model model) {
        if (ex instanceof InvocationTargetException) {
            ex = ((InvocationTargetException) ex).getTargetException();
        }
        logger.error("can not resolve request from [ip:{}] to [url:{}] cause:{}", request.getRemoteAddr(), request.getRequestURL(), ex.getMessage(), ex);
        if (request.getContentType() != null && (request.getContentType().contains(MediaType.TEXT_HTML_VALUE))) {
            if (ex instanceof ServiceException) {
                model.addAttribute(ERR_MSG, ex.getMessage());
            }
            return "error";
        } else {
            if (ex instanceof ShiroException) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject(ResultCode.AUTHORIZE_ERROR));
            } else if (ex instanceof ServiceException) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject(((ServiceException) ex).getCode(), ex.getMessage()));
            }
            if (EnvConfig.isDebug()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject(ResultCode.SERVER_ERROR.getCode(), ex.getMessage()));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject(ResultCode.SERVER_ERROR));
            }
        }
    }
}
