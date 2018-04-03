package dna.origins.util.encrypt;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class HashEncryptUtils {

    /**
     * 传入文本内容，返回 MD5-128 串
     *
     * @param strText
     * @return
     */
    public static String MD5(final String strText) {
        return Encrypt(strText, "MD5");
    }

    /**
     * 传入文本内容，返回 SHA-128 串
     *
     * @param strText
     * @return
     */
    public static String SHA1(final String strText) {
        return Encrypt(strText, "SHA-1");
    }

    /**
     * 传入文本内容，返回 SHA-256 串
     *
     * @param strText
     * @return
     */
    public static String SHA256(final String strText) {
        return Encrypt(strText, "SHA-256");
    }

    /**
     * 传入文本内容，返回 SHA-512 串
     *
     * @param strText
     * @return
     */
    public static String SHA512(final String strText) {
        return Encrypt(strText, "SHA-512");
    }

    /**
     * 字符串 Digest 加密
     *
     * @param strText
     * @param strType
     * @return
     */
    private static String Encrypt(final String strText, final String strType) {
        // 返回值
        String strResult = null;

        // 是否是有效字符串
        if (strText != null && strText.length() > 0) {
            try {
                // Digest 加密开始
                // 创建加密对象 并傳入加密類型
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                // 传入要加密的字符串
                messageDigest.update(strText.getBytes());
                // 得到 byte 類型结果
                byte byteBuffer[] = messageDigest.digest();

                // 將 byte 轉換爲 string
                StringBuffer strHexString = new StringBuffer();
                // 遍歷 byte buffer
                for (int i = 0; i < byteBuffer.length; i++) {
                    String hex = Integer.toHexString(0xff & byteBuffer[i]);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                // 得到返回結果
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        return strResult;
    }

    public static void main(String[] args) throws Exception {
        Random RANDOM = new SecureRandom();
        char[] codeSequence = {'a', 'b', 'c', 'd', 'e', 'f', '2', '3', '4', '5', '6', '7', '8', '9'};
        StringBuffer appId = new StringBuffer("anti");
        for (int i = 0; i < 14; i++) {
            char n = codeSequence[RANDOM.nextInt(14)];
            appId.append(n);
        }
        //String str = new String(salt, "UTF-8");
        String appSecret = RandomStringUtils.randomAlphanumeric(32);
        //String str = new BASE64Encoder().encode(salt);
        System.out.println(appId);
        System.out.println(appSecret);
    }
}
