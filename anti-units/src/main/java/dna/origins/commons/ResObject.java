package dna.origins.commons;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.springframework.http.HttpStatus;

public class ResObject<T> {
    private T body;
    private Integer code;
    private String msg;

    public ResObject() {
    }

    public ResObject(Integer code, String msg, T body) {
        this.code = code;
        this.msg = msg;
        this.body = body;
    }

    public ResObject(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    public ResObject(ResultCode resultCode, T body) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
        this.body = body;
    }

    public ResObject(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResObject(HttpStatus status) {
        this.code = status.value();
        this.msg = status.getReasonPhrase();
    }

    public ResObject(HttpStatus status, T body) {
        this.code = status.value();
        this.msg = status.getReasonPhrase();
        this.body = body;
    }

    @JSONField(serialize = false)
    public boolean isSuccess() {
        if (code != null && code < 600) {//600以上为自定义状态码
            HttpStatus.Series series = HttpStatus.Series.valueOf(code);
            switch (series) {
                case CLIENT_ERROR:
                case SERVER_ERROR:
                    return false;
                default:
                    return true;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

    public T getBody() {
        return body;
    }

    public void setBody(T body) {
        this.body = body;
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
