package dna.origins.util.encrypt;


import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class HmacUtils {
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    public static String HmacSHA256(String key, String content) {
        return digest(HMAC_SHA256_ALGORITHM, key, content);
    }

    public static String HmacSHA1(String key, String content) {
        return digest(HMAC_SHA1_ALGORITHM, key, content);
    }

    public static String digest(String nameSpace, String key, String content) {
        try {
            Mac mac = Mac.getInstance(nameSpace);
            byte[] secretByte = key.getBytes("utf-8");
            byte[] dataBytes = content.getBytes("utf-8");

            SecretKey secret = new SecretKeySpec(secretByte, nameSpace);
            mac.init(secret);

            byte[] doFinal = mac.doFinal(dataBytes);
            return new String(Base64.encodeBase64(doFinal));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

