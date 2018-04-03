package dna.origins.util.authorization;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.Map;

public class TokenUtils {

    public static final String URT = "$urt";

    public static String getJWTString(String sub, Date expires, Key key, String id, Map<String, Object> claims) {
        if (sub == null) {
            throw new NullPointerException("null username is illegal");
        }
        if (expires == null) {
            throw new NullPointerException("null expires is illegal");
        }
        if (key == null) {
            throw new NullPointerException("null key is illegal");
        }
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        String jwtString = Jwts.builder().setClaims(claims)
                .setIssuer("Jersey-Security-Basic")
                .setSubject(sub)
                .setAudience("anti-web")
                .setExpiration(expires)
                .setIssuedAt(new Date())
                .setId(id)
                .signWith(signatureAlgorithm, key)
                .compact();
        return jwtString;
    }

    public static boolean isValid(String token, Key key) {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token.trim());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String updateToken(Claims claims, Key key, Date expiry) {
        if (key == null) {
            throw new NullPointerException("null key is illegal");
        }
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        claims.setExpiration(expiry);
        String jwtString = Jwts.builder()
                .setClaims(claims)
                .signWith(signatureAlgorithm, key)
                .compact();
        return jwtString;
    }

    public static Claims getClaims(String jwsToken, Key key) {
        try {
            Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwsToken.trim()).getBody();
            return claims;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getName(String jwsToken, Key key) {
        if (isValid(jwsToken, key)) {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(jwsToken);
            String name = String.valueOf(claimsJws.getBody().get("name"));
            return name;
        }
        return null;
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader(" Proxy-Client-IP ");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static int getVersion(String jwsToken, Key key) {
        if (isValid(jwsToken, key)) {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(jwsToken);
            return Integer.parseInt(claimsJws.getBody().getId());
        }
        return -1;
    }

    /**
     * @param @param  jwsToken
     * @param @param  key
     * @param @return 设定文件
     * @return String    返回类型
     * @throws
     * @Title: getCompanyCode
     */
    public static String getCompanyId(String jwsToken, Key key) {
        if (isValid(jwsToken, key)) {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(jwsToken);
            String companyid = String.valueOf(claimsJws.getBody().get("id"));
            return companyid;
        }
        return null;
    }

    /**
     * @param @param  accessToken
     * @param @param  key
     * @param @return 设定文件
     * @return String    返回类型
     * @throws
     * @Title: setAcccessToken
     */
    public static void setAcccessToken(String authToken, Key key, String accessToken) {
        if (isValid(authToken, key)) {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(authToken);
            claimsJws.getBody().put("acessToken", accessToken);
        }
    }

    /**
     * @param @param  jwsToken
     * @param @param  key
     * @param @return 设定文件
     * @return String    返回类型
     * @throws
     * @Title: getCompanyCode
     */
    public static String getAccessToken(String jwsToken, Key key) {
        if (isValid(jwsToken, key)) {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(jwsToken);
            return claimsJws.getBody().getSubject();
        }
        return null;
    }

}
