package dna.persistence.shiro;

import dna.origins.commons.IPermission;
import dna.origins.commons.IRole;
import dna.origins.commons.SimplePrincipal;
import dna.persistence.factory.AppContextHolder;
import dna.persistence.factory.ContextUtil;
import dna.persistence.hibernate.SessionFactoryHolder;
import dna.persistence.regulate.DictionaryService;
import dna.persistence.template.Context;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.springframework.util.ObjectUtils;

import javax.validation.constraints.NotNull;
import java.util.List;

public class UserRoleToken<R extends IRole, P extends IPermission> implements AuthenticationToken {
    private static final long serialVersionUID = 8822251820152784472L;
    //CAUTION: 此处写法有待研究
    public static final String CID_BASE = AppContextHolder.getBean("sessionFactoryHolder", SessionFactoryHolder.class).getPrimaryDB();
    public static final String CNAME_BASE = "杭州网加信息技术有限公司";
    private Integer userId;
    private String userName;
    private String realName;
    private Integer userType;
    private Integer targetId;
    private String companyCode;
    private String companyName;
    private List<R> roles;
    private List<P> permissions;
    private String uuid;
    private String urtKey;
    private boolean enable = true;
    private Integer clientType;

    @Override
    public Object getPrincipal() {
        return new SimplePrincipal(userId, companyCode);
    }

    @Override
    public Object getCredentials() {
        return urtKey;
    }

    public UserRoleToken() {
    }

    public UserRoleToken(Integer userId, String userName, String realName, String companyCode, String companyName, Integer clientType, Integer userType, Integer targetId) {
        this.userId = userId;
        this.userName = userName;
        this.realName = realName;
        this.companyCode = StringUtils.isBlank(companyCode) ? CID_BASE : companyCode.trim();
        this.companyName = companyName;
        this.clientType = clientType == null ? 0 : clientType;
        this.userType = userType;
        this.targetId = targetId;
    }

    public Boolean isValid() {
        return !(this.userId == null || StringUtils.isBlank(this.userName) || StringUtils.isBlank(this.uuid) || StringUtils.isBlank(this.urtKey) || ObjectUtils.isEmpty(this.roles) || ObjectUtils.isEmpty(this.permissions));
    }

    @NotNull
    public static UserRoleToken getCurrentUrt() {
        UserRoleToken urt = ContextUtil.get(Context.URT, UserRoleToken.class);
        return urt;
    }

    public static String generateUrtKey(String cid, String userName, Integer clientType) {
        cid = StringUtils.isBlank(cid) ? CID_BASE : cid;
        return cid.concat("$").concat(userName).concat("_".concat(DictionaryService.getDictionaryValue("base/dictionary/ClientType.dic", clientType == null ? 0 : clientType))).concat("^".concat(RandomStringUtils.randomAscii(4)));
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public List<? extends IRole> getRoles() {
        return roles;
    }

    public void setRoles(List<R> roles) {
        this.roles = roles;
    }

    public List<? extends IPermission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<P> permissions) {
        this.permissions = permissions;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUrtKey() {
        return urtKey;
    }

    public void setUrtKey(String urtKey) {
        this.urtKey = urtKey;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Integer getClientType() {
        return clientType;
    }

    public void setClientType(Integer clientType) {
        this.clientType = clientType;
    }
}
