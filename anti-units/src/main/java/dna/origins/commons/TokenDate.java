package dna.origins.commons;

import org.joda.time.DateTime;

import java.util.Date;

public class TokenDate {
    private String token;
    private Date expiresIn;
    private TokenType type;//1：第三方平台 2：公众号

    public TokenDate() {
    }

    public TokenDate(String token, Date expiresIn, TokenType type) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.type = type;
    }

    public TokenDate(String token, int expiresIn, TokenType type) {
        this.token = token;
        this.expiresIn = new DateTime().plusSeconds(expiresIn).toDate();
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Date expiresIn) {
        this.expiresIn = expiresIn;
    }

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public boolean isValid() {
        return expiresIn != null && expiresIn.after(new Date());
    }
}
