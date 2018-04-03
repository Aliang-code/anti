package dna.origins.util.encrypt;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;


public class CredenceScaler {

    /**
     * 凭证生成
     *
     * @param plaintext md5密码
     * @return
     */
    public static String[] generateCredence(String plaintext) {
        if (StringUtils.isBlank(plaintext)) {
            throw new IllegalArgumentException("input for plaintext can not be blank");
        }
        String[] result = new String[2];
        result[0] = RandomStringUtils.randomAlphanumeric(48);
        result[1] = HashEncryptUtils.SHA512(result[0] + plaintext);
        return result;
    }
}
