package dna.origins.util.toolbar;

import com.alibaba.fastjson.JSONObject;
import dna.origins.commons.SignType;
import dna.origins.util.encrypt.HashEncryptUtils;
import dna.origins.util.encrypt.HmacUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.TreeMap;

public class HttpConversion {
    public static final String SEPARATOR = "&";
    public static final String PARAM_SIGN = "sign";
    public static final String PARAM_NONCE_STR = "nonce_str";

    public static String generateSign(String key, JSONObject data, SignType signType) {
        StringBuilder sb = new StringBuilder();
        String sign = "";
        if (data.get(PARAM_NONCE_STR) == null) {
            String nonceStr = RandomStringUtils.randomAlphanumeric(32);
            data.put("nonce_str", nonceStr);
        }
        Map<String, Object> map = new TreeMap<>(data);
        for (String k : map.keySet()) {
            if (data.get(k) == null || k.equals(PARAM_SIGN)) {
                continue;
            }
            if (sb.length() != 0) {
                sb.append(SEPARATOR);
            }
            sb.append(k).append("=").append(data.getString(k));
        }
        sb.append(SEPARATOR).append("key=").append(key);
        switch (signType) {
            case ANTI:
                sign = HmacUtils.HmacSHA256(key, sb.toString()).toUpperCase();
                break;
            case WX_PAY:
                sign = HashEncryptUtils.MD5(sb.toString()).toUpperCase();
                break;
            default:
                break;
        }
        return sign;
    }

    public static boolean verifySign(String key, JSONObject data, SignType signType) {
        String sign = (String) data.get(PARAM_SIGN);
        if (StringUtils.isBlank(sign)) {
            return false;
        }
        String verifySign = generateSign(key, data, signType);
        if (sign.equals(verifySign)) {
            return true;
        } else {
            return false;
        }
    }
}
