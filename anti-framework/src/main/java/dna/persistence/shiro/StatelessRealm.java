package dna.persistence.shiro;

import dna.origins.commons.IPermission;
import dna.origins.commons.IRole;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

import java.util.HashSet;
import java.util.Set;

public class StatelessRealm extends AuthorizingRealm {

    public boolean supports(AuthenticationToken token) {
        //仅支持UserRoleToken类型的Token
        return token instanceof UserRoleToken;
    }

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //根据用户名查找角色
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        UserRoleToken<IRole, IPermission> urt = UserRoleToken.getCurrentUrt();
        if (urt != null && urt.isValid() && urt.isEnable()) {
            Set<String> roleNames = new HashSet<>();
            urt.getRoles().forEach(role -> roleNames.add(role.getName()));
            Set<String> permissionNames = new HashSet<>();
            urt.getPermissions().forEach(permission -> permissionNames.add(permission.getName()));
            authorizationInfo.setRoles(roleNames);
            authorizationInfo.setStringPermissions(permissionNames);
        }
        return authorizationInfo;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        //存入凭证
        return new SimpleAuthenticationInfo(
                token.getPrincipal(),
                token.getCredentials(),
                getName());
    }

}