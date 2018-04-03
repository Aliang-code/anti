package dna.origins.commons;

public class SimplePrincipal {
    private Integer userId;
    private String companyCode;

    public SimplePrincipal(Integer userId, String companyCode) {
        this.userId = userId;
        this.companyCode = companyCode;
    }

    public Integer getUserName() {
        return userId;
    }

    public void setUserName(Integer userName) {
        this.userId = userName;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }
}
