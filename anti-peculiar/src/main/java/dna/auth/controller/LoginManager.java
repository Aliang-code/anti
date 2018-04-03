package dna.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dna.auth.service.LoginService;
import dna.entity.base.LoginInfo;
import dna.origins.commons.*;
import dna.origins.util.authorization.CookieUtils;
import dna.origins.util.authorization.TokenUtils;
import dna.origins.util.constants.EhcacheConstant;
import dna.origins.util.constants.JWTConstant;
import dna.origins.util.toolbar.DateConversion;
import dna.persistence.shiro.UserRoleToken;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.*;

import static dna.persistence.shiro.UserRoleToken.CID_BASE;

@CrossOrigin(origins = "*", exposedHeaders = {JWTConstant.TOKEN_HEAD_NAME})
@Controller
public class LoginManager {
    private static final Logger logger = LoggerFactory.getLogger(LoginManager.class);

    @Autowired
    private LoginService loginService;

    @PostMapping(value = "/login")
    public ResponseEntity loginUser(@RequestBody LoginInfo loginInfo, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> loginResultInfo = loginService.loginForCompany(loginInfo);
        ResultCode loginResult = (ResultCode) loginResultInfo.get("loginCode");
        if (loginResult.equals(ResultCode.LOGIN_SUCCESS)) {
            //查询用户信息
            UserRoleToken<IRole, IPermission> urt = UserRoleToken.getCurrentUrt();
            if (null == urt || !urt.isValid()) {
                //用户不存在
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject(ResultCode.USERINFO_MISSING));
            }
            String uuid = urt.getUuid();
            String urtKey = urt.getUrtKey();
            //组装缓存数据
            String userName = urt.getUserName();
            Map<String, Object> claims = new HashMap<>();
            claims.put(TokenUtils.URT, urtKey);
            // 设置这个token的生命时间
            Date expiry;
            if (loginInfo.getClientType() == null || loginInfo.getClientType() == 0) {
                expiry = DateConversion.getDateAftMinute(new Date(), 30);//30分钟的有效日期
            } else {
                expiry = DateConversion.getDateAftXDays(new Date(), 7);
            }
            // 使用Token工具类得到token，生成的策略是利用用户的姓名，到期时间，和私钥
            Key key = MacProvider.generateKey(SignatureAlgorithm.HS512);
            EhcacheUtils.instance().put(EhcacheConstant.KEY_CACHE_NAME, uuid, key);
            EhcacheUtils.instance().put(EhcacheConstant.TOKEN_CACHE_NAME, urtKey, urt);
            // HS256签名算法
            String jwtToken = TokenUtils.getJWTString(TokenUtils.getIpAddr(request), expiry, key, uuid, claims);
            logger.debug("user[{}] login Success, Token is:{} loginInfo is :{}", userName, jwtToken, JSON.toJSON(loginInfo));

            Map<String, Object> dataMap = new HashMap<>();
            List<String> roles = new ArrayList<>();
            List<String> pms = new ArrayList<>();
            //JSONObject pmJson = loginService.formatPermission(urt.getPermissions());
            for (IRole r : urt.getRoles()) {
                roles.add(r.getName());
            }
            for (IPermission p : urt.getPermissions()) {
                pms.add(p.getName());
            }
            dataMap.put("userId", urt.getUserId());
            dataMap.put("userName", userName);
            dataMap.put("realName", urt.getRealName());
            dataMap.put("userType", urt.getUserType());
            dataMap.put("companyName", urt.getCompanyName());
            dataMap.put("cid", StringUtils.isBlank(urt.getCompanyCode()) ? CID_BASE : urt.getCompanyCode());
            dataMap.put("roles", roles);
            dataMap.put("permissions", pms);
            if (loginInfo.getCookieEnabled()) {
                Cookie cookie_tk = CookieUtils.createCookie(JWTConstant.TOKEN_COOIKE_NAME, jwtToken);
                Cookie cookie_uuid = CookieUtils.createCookie(JWTConstant.UUID_COOIKE_NAME, uuid);
                response.addCookie(cookie_tk);
                response.addCookie(cookie_uuid);
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject<>(loginResult, dataMap));
            } else {
                return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON_UTF8).header(JWTConstant.UUID_HEAD_NAME, uuid).
                        header(JWTConstant.TOKEN_HEAD_NAME, jwtToken).body(new ResObject<>(loginResult, dataMap));
            }
        } else {
            logger.error("The responseJsonObject:{} for login IP[{}]", JSONObject.toJSONString(loginResult), request.getRemoteAddr());
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject(loginResult));
        }
    }

    @PostMapping(value = "/auth/logout")
    public ResponseEntity logout(HttpServletRequest request, HttpServletResponse response) throws Exception {
        UserRoleToken urt = UserRoleToken.getCurrentUrt();
        EhcacheUtils.instance().remove(EhcacheConstant.KEY_CACHE_NAME, urt.getUuid());
        EhcacheUtils.instance().remove(EhcacheConstant.TOKEN_CACHE_NAME, urt.getUrtKey());
        Cookie cookie_uuid = CookieUtils.getCookieByName(request, JWTConstant.UUID_COOIKE_NAME);
        Cookie cookie_tk = CookieUtils.getCookieByName(request, JWTConstant.TOKEN_COOIKE_NAME);
        cookie_tk.setMaxAge(0);
        cookie_uuid.setMaxAge(0);
        response.addCookie(cookie_tk);
        response.addCookie(cookie_uuid);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject<>(ResultCode.REQUESST_SUCCESS, true));
    }

    @PostMapping(value = "/auth/heartbeat")
    public ResponseEntity heartbeat() throws Exception {
        UserRoleToken<IRole, IPermission> urt = UserRoleToken.getCurrentUrt();
        Map<String, Object> dataMap = new HashMap<>();
        List<String> roles = new ArrayList<>();
        List<String> pms = new ArrayList<>();
        //JSONObject pmJson = loginService.formatPermission(urt.getPermissions());
        for (IRole r : urt.getRoles()) {
            roles.add(r.getName());
        }
        for (IPermission p : urt.getPermissions()) {
            pms.add(p.getName());
        }
        dataMap.put("userId", urt.getUserId());
        dataMap.put("userName", urt.getUserName());
        dataMap.put("realName", urt.getRealName());
        dataMap.put("userType", urt.getUserType());
        dataMap.put("companyName", urt.getCompanyName());
        dataMap.put("cid", StringUtils.isBlank(urt.getCompanyCode()) ? CID_BASE : urt.getCompanyCode());
        dataMap.put("roles", roles);
        dataMap.put("permissions", pms);
        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON_UTF8).body(new ResObject<>(ResultCode.REQUESST_SUCCESS, dataMap));
    }
}
