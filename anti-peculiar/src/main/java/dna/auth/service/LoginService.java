package dna.auth.service;

import com.alibaba.fastjson.JSONObject;
import dna.auth.dao.LoginInfoDAO;
import dna.entity.base.LoginInfo;
import dna.entity.base.Permission;
import dna.entity.base.Role;
import dna.entity.base.User;
import dna.origins.commons.EhcacheUtils;
import dna.origins.commons.IRole;
import dna.origins.commons.ResultCode;
import dna.origins.util.constants.EhcacheConstant;
import dna.origins.util.encrypt.HashEncryptUtils;
import dna.persistence.factory.ContextUtil;
import dna.persistence.hibernate.SessionFactoryHolder;
import dna.persistence.shiro.UserRoleToken;
import dna.persistence.template.Context;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.security.Key;
import java.util.*;

@Service
public class LoginService {
    private static final String LOGINCODE = "loginCode";

    @Autowired
    private LoginInfoDAO loginInfoDAO;

    public Boolean checkMD5PWD(String userName, String md5pwd) {
        UserRoleToken urt = UserRoleToken.getCurrentUrt();
        String cid = urt.getCompanyCode();
        User user = loginInfoDAO.findPwdInfoByUserName(userName, cid);
        return !ObjectUtils.isEmpty(user) && HashEncryptUtils.SHA512(user.getSalt() + md5pwd).equals(user.getPassword());
    }

    public Map<String, Object> loginForCompany(LoginInfo loginInfo) {
        Map<String, Object> loginResult = new HashMap<>();
        String userName = loginInfo.getName();
        String cid = loginInfo.getCid();
        Integer clientType = loginInfo.getClientType();
        if(!SessionFactoryHolder.hasSessionFactory(cid)){
            loginResult.put(LOGINCODE, ResultCode.USER_NOT_FOUND);
            return loginResult;
        }
        String companyName = loginInfoDAO.getCompanyName(cid);
        if (companyName == null) {
            loginResult.put(LOGINCODE, ResultCode.COMPANY_DISABLE);
            return loginResult;
        }
        User user = loginInfoDAO.findPwdInfoByUserName(userName, cid);
        if (ObjectUtils.isEmpty(user)) {
            loginResult.put(LOGINCODE, ResultCode.USER_NOT_FOUND);
        } else {
            if (HashEncryptUtils.SHA512(user.getSalt() + loginInfo.getPwd()).equals(user.getPassword())) {
                if (loginInfo.getClientType() != null && loginInfo.getClientType() != 0) {//设备登录
                    if (user.getUserType() == 0 || (loginInfo.getClientType() == 1 && user.getUserType() > 2)) {
                        loginResult.put(LOGINCODE, ResultCode.AUTHORIZE_NOT_MATCH);
                        return loginResult;
                    } else if (user.getTargetId() == null) {
                        loginResult.put(LOGINCODE, ResultCode.USERINFO_MISSING);
                        return loginResult;
                    }
                }
                UserRoleToken<Role, Permission> urt = new UserRoleToken<>(user.getId(), userName, user.getRealName(), cid, companyName, clientType, user.getUserType(), user.getTargetId());
                urt.setRoles(loginInfoDAO.findRoleByUserName(userName, cid));
                List<Integer> roleIds = new ArrayList<>();
                for (IRole role : urt.getRoles()) {
                    roleIds.add(role.getId());
                }
                if(roleIds.isEmpty()){
                    loginResult.put(LOGINCODE,ResultCode.USERINFO_MISSING);
                    return loginResult;
                }
                urt.setPermissions(loginInfoDAO.findPermissionByRoleId(roleIds, cid));
                urt.setUuid(loginInfo.getUuid());
                //String sUuid = UUID.randomUUID().toString().replaceAll("-", "");
                String urtKey = UserRoleToken.generateUrtKey(cid, userName, clientType);
                urt.setUrtKey(urtKey);
                if (urt.isValid()) {
                    ContextUtil.put(Context.URT, urt);
                    loginResult.put(LOGINCODE, ResultCode.LOGIN_SUCCESS);
                } else {
                    loginResult.put(LOGINCODE, ResultCode.USERINFO_MISSING);
                }
                //loginResult.put("mobile",user.getMobile);
            } else {
                loginResult.put(LOGINCODE, ResultCode.USERINFO_ERROR);
            }
        }
        return loginResult;
    }

    /**
     * 更新已登录用户信息
     *
     * @param userIds
     */
    public void updateLoginUserInfo(List<Integer> userIds) {
        List<Object> urtList = EhcacheUtils.instance().getAll(EhcacheConstant.TOKEN_CACHE_NAME);
        String cid = UserRoleToken.getCurrentUrt().getCompanyCode();
        urtList.forEach(u -> {
            UserRoleToken urt = (UserRoleToken) u;
            if (cid.equals(urt.getCompanyCode()) && userIds.contains(urt.getUserId())) {
                urt.setEnable(false);
                EhcacheUtils.instance().put(EhcacheConstant.TOKEN_CACHE_NAME, urt.getUrtKey(), urt);
            }
        });
    }

    public JSONObject formatPermission(List<Permission> permissions) {
        return formatPermission(permissions, 0);
    }

    private JSONObject formatPermission(List<Permission> permissions, int parentId) {
        Iterator<Permission> iterator = permissions.iterator();
        JSONObject formatPm = new JSONObject();
        while (iterator.hasNext()) {
            Permission child = iterator.next();
            if (child.getParentId() == parentId) {
                JSONObject childJson = new JSONObject();
                childJson.put("value", true);
                iterator.remove();
                childJson.put("child", formatPermission(permissions, child.getId()));
                iterator = permissions.iterator();
                String[] spName = child.getName().split(":");
                formatPm.put(spName[spName.length - 1], childJson);
            }
        }
        return formatPm;
    }

    public static void main(String[] args) {
        String pw = HashEncryptUtils.MD5("123");
        String saltps = HashEncryptUtils.SHA512("11" + pw);
        Key key = MacProvider.generateKey(SignatureAlgorithm.HS512);
        System.out.println(pw + "---" + saltps + "  " + key.getEncoded());
        List<String> strList=new ArrayList<String>(){{add("123");add("456");}};
        List<Integer> numList=new ArrayList<Integer>(){{add(11);add(22);}};
        String[] strArrays=new String[]{"123","456"};
        Integer[] numArrays=new Integer[]{11,22};
        System.out.println("strList:"+JSONObject.toJSONString(strList));
        System.out.println("numList:"+JSONObject.toJSONString(numList));
        System.out.println("strArrays:"+JSONObject.toJSONString(strArrays));
        System.out.println("numArrays:"+JSONObject.toJSONString(numArrays));
    }
}
