package dna.persistence.shiro;

import dna.persistence.factory.ContextUtil;
import dna.persistence.template.Context;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class ApplicationRealm extends AuthorizingRealm {

    public boolean supports(AuthenticationToken token) {
        return token instanceof ApplicationToken;
    }

    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.setRoles(ApplicationToken.roleName);
        authorizationInfo.setStringPermissions(ApplicationToken.permissionNames);
        return authorizationInfo;
    }

    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        ContextUtil.put(Context.URT, ((ApplicationToken) token).generateUrt());
        return new SimpleAuthenticationInfo(
                token.getPrincipal(),
                token.getCredentials(),
                getName());
    }
}
