package dna.persistence.shiro;

import dna.origins.commons.SimplePrincipal;
import org.apache.shiro.authc.AuthenticationToken;

import javax.validation.constraints.NotNull;
import java.util.Set;

public class ApplicationToken implements AuthenticationToken {
    private static final Integer userId = 999;
    private String companyCode;
    public static Set<String> roleName;
    public static Set<String> permissionNames;

    public ApplicationToken(@NotNull String companyCode) {
        this.companyCode = companyCode;
    }

    public UserRoleToken generateUrt() {
        UserRoleToken urt = new UserRoleToken();
        urt.setUserId(userId);
        urt.setCompanyCode(companyCode);
        return urt;
    }

    @Override
    public Object getPrincipal() {
        return new SimplePrincipal(userId, companyCode);
    }

    @Override
    public Object getCredentials() {
        return companyCode.concat("$").concat(String.valueOf(userId));
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }
}
