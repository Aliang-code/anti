package dna.auth.config;

import com.alibaba.fastjson.JSON;
import dna.auth.controller.LoginManager;
import dna.origins.commons.EhcacheUtils;
import dna.origins.commons.ResObject;
import dna.origins.commons.ResultCode;
import dna.origins.commons.ServiceException;
import dna.origins.util.authorization.CookieUtils;
import dna.origins.util.authorization.TokenUtils;
import dna.origins.util.constants.EhcacheConstant;
import dna.origins.util.constants.JWTConstant;
import dna.origins.util.toolbar.DateConversion;
import dna.persistence.factory.AppContextHolder;
import dna.persistence.factory.ContextUtil;
import dna.persistence.shiro.UserRoleToken;
import dna.persistence.template.Context;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.util.Date;

public class StatelessAuthFilter extends AccessControlFilter {
    public static final Logger logger = LoggerFactory.getLogger(StatelessAuthFilter.class);

    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        return false;
    }

    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return true;
        }
        try {
            String requestURL = request.getRequestURL().toString();
            String currentIP = TokenUtils.getIpAddr(request);
            Cookie cookie_uuid = CookieUtils.getCookieByName(request, JWTConstant.UUID_COOIKE_NAME);
            Cookie cookie_tk = CookieUtils.getCookieByName(request, JWTConstant.TOKEN_COOIKE_NAME);
            Boolean cookieEnable = cookie_tk != null && cookie_uuid != null;
            String jwtToken = cookieEnable ? cookie_tk.getValue() : request.getHeader(JWTConstant.TOKEN_HEAD_NAME);
            String uuid = cookieEnable ? cookie_uuid.getValue() : request.getHeader(JWTConstant.UUID_HEAD_NAME);
            if (StringUtils.isBlank(uuid) || StringUtils.isBlank(jwtToken)) {
                throw new AuthenticationException("authInfo is missing from [ip:" + currentIP + "] to [url:" + requestURL + "]");
            }
            logger.debug("client[ip:{}] submit token[{}] to [url:{}]", currentIP, jwtToken, requestURL);
            Key key = (Key) EhcacheUtils.instance().get(EhcacheConstant.KEY_CACHE_NAME, uuid);
            if (key == null) {
                throw new AuthenticationException("illegal uuid[" + uuid + "] from [ip:" + currentIP + "] to [url:" + requestURL + "]");
            }
            Claims claims = TokenUtils.getClaims(jwtToken, key);
            if (claims != null) {
                String urt_key = claims.get(TokenUtils.URT, String.class);
                UserRoleToken urt = (UserRoleToken) EhcacheUtils.instance().get(EhcacheConstant.TOKEN_CACHE_NAME, urt_key);
                String authIP = claims.getSubject();
                if (urt != null && urt.isValid() && ((JWTConstant.LOCALHOST_IP.contains(currentIP) && JWTConstant.LOCALHOST_IP.contains(authIP)) || currentIP.equals(authIP))) {
                    ContextUtil.put(Context.URT, urt);
                    if (urt.isEnable()) {
                        getSubject(request, response).login(urt);
                        Date expiry;
                        if ((urt.getClientType() == 0 && claims.getExpiration().before(DateConversion.getDateAftMinute(new Date(), 30))) ||
                                (urt.getClientType() != 0 && claims.getExpiration().before(DateConversion.getDateAftXDays(new Date(), 3))) ||
                                requestURL.contains("heartbeat")) {
                            if (urt.getClientType() == 0) {
                                expiry = DateConversion.getDateAftMinute(new Date(), 60);//30分钟的有效日期
                            } else {
                                expiry = DateConversion.getDateAftXDays(new Date(), 7);
                            }
                            jwtToken = TokenUtils.updateToken(claims, key, expiry);
                            //response.setHeader(JWTConstant.UUID_HEAD_NAME, uuid);
                            //if (requestURL.contains("auth")) {
                                if (cookieEnable) {
                                    cookie_tk.setValue(jwtToken);
                                    response.addCookie(cookie_tk);
                                } else {
                                    response.setHeader(JWTConstant.TOKEN_HEAD_NAME, jwtToken);
                                }
                            //}
                        }
                    } else {
                        AppContextHolder.getBean("loginManager", LoginManager.class).logout(request, response);
                        throw new ServiceException("expired authToken from [ip:" + currentIP + "] to [url:" + requestURL + "]");
                    }
                } else {
                    throw new AuthenticationException("illegal authToken from [ip:" + currentIP + "] to [url:" + requestURL + "] with uuid[" + uuid + "],userRoleToken[" + JSON.toJSONString(urt) + "],urt_key[" + urt_key + "]");
                }
            } else {
                throw new AuthenticationException("illegal authToken from [ip:" + currentIP + "] to [url:" + requestURL + "]");
            }

            return true;
        } catch (Exception e) {
            logger.warn("onAccessDenied error:{}", e.getMessage());
            dealErrorReturn(request, response, e);
            return false;
        }
    }

    public static String extractJwtTokenFromAuthorizationHeader(String auth) {
        //Replacing "Bearer Token" to "Token" directly
        return auth.replaceFirst("[B|b][E|e][A|a][R|r][E|e][R|r] ", "").replace(" ", "");
    }

    // 处理鉴权异常
    public void dealErrorReturn(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Exception e) {
        String json;
        PrintWriter writer = null;
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, httpServletRequest.getHeader(HttpHeaders.ORIGIN));
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "POST");
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, JWTConstant.TOKEN_HEAD_NAME + "," + JWTConstant.UUID_HEAD_NAME);
        httpServletResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        if (e instanceof ServiceException) {
            httpServletResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            json = JSON.toJSONString(new ResObject(ResultCode.AUTHORIZE_EXPIRED));
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            json = JSON.toJSONString(new ResObject(ResultCode.AUTHORIZE_ERROR));
        }
        try {
            writer = httpServletResponse.getWriter();
            writer.print(json);
        } catch (IOException ex) {
            logger.error("response error:", ex);
        } finally {
            if (writer != null)
                writer.close();
        }
    }
}
