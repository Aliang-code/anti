package dna.origins.commons;


public enum ResultCode {
    LOGIN_SUCCESS(200, "登陆成功！"), REQUESST_SUCCESS(200, null),
    NOT_FOUND(400, "请求失败！"),
    AUTHORIZE_ERROR(401, "无效认证请求！"), AUTHORIZE_EXPIRED(403, "用户授权信息已变更，请重新登录！"),
    USER_NOT_FOUND(609, "用户不存在！"), USERINFO_ERROR(609, "用户名或密码错误！"), USERINFO_MISSING(609, "用户信息无效，请联系管理员！"),
    AUTHORIZE_NOT_MATCH(609, "用户无此设备操作权限！"), COMPANY_DISABLE(609, "该企业正在维护中！"),
    SERVER_ERROR(500, "请求执行失败！");

    private Integer code;
    private String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static String getMsg(Integer code) {
        for (ResultCode c : ResultCode.values()) {
            if (c.getCode() == code) {
                return c.msg;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
