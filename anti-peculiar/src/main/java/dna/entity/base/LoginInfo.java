package dna.entity.base;

public class LoginInfo {
    private String cid;
    private String name;
    private String pwd;
    private String uuid;
    private Boolean cookieEnabled;
    private Integer clientType;

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }


    public Boolean getCookieEnabled() {
        return cookieEnabled;
    }

    public void setCookieEnabled(Boolean cookieEnabled) {
        this.cookieEnabled = cookieEnabled;
    }

    public Integer getClientType() {
        return clientType;
    }

    public void setClientType(Integer clientType) {
        this.clientType = clientType;
    }
}
