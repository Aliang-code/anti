package dna.origins.util.encrypt;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class CipherScaler {
    private static final Logger logger = LoggerFactory.getLogger(CipherScaler.class);
    /**
     * 注意key和加密用到的字符串是不一样的 加密还要指定填充的加密模式和填充模式 AES密钥可以是128或者256，加密模式包括ECB, CBC等
     * ECB模式是分组的模式，CBC是分块加密后，每块与前一块的加密结果异或后再加密 第一块加密的明文是与IV变量进行异或
     */
    public static final String KEY_ALGORITHM = "AES";
    public static final String ECB_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    public static final String CBC_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String DB_KEY = "AFIJHSDFNXCMVUER";
    public static final String PLAIN_TEXT = "MANUTD is the greatest club in the world";

    /**
     * IV(Initialization Value)是一个初始值，对于CBC模式来说，它必须是随机选取并且需要保密的
     * 而且它的长度和密码分组相同(比如：对于AES 128为128位，即长度为16的byte类型数组)
     */
    public static final byte[] IVPARAMETERS = new byte[]{1, 2, 3, 4, 5, 6, 7,
            8, 9, 10, 11, 12, 13, 14, 15, 16};

    public static void main(String[] arg) throws Exception {
        SecretKeySpec key = new SecretKeySpec(DB_KEY.getBytes(), "AES");
        String t = "dnaanti_2017";
        byte[] encodeText1 = AesCbcEncode(t.getBytes(), key, IVPARAMETERS);
        System.out.print(Base64.encodeBase64String(encodeText1));
    }

    public static String decode(String context) {
        SecretKeySpec key = new SecretKeySpec(DB_KEY.getBytes(), "AES");
        return AesCbcDecode(Base64.decodeBase64(context.getBytes()), key, IVPARAMETERS);
    }

    /**
     * 使用ECB模式进行加密。 加密过程三步走： 1. 传入算法，实例化一个加解密器 2. 传入加密模式和密钥，初始化一个加密器 3.
     * 调用doFinal方法加密
     *
     * @param plainText
     * @return
     */
    public static byte[] AesEcbEncode(byte[] plainText, SecretKey key) {

        try {

            Cipher cipher = Cipher.getInstance(ECB_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(plainText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            logger.error("", e);
        }
        return null;
    }

    /**
     * 使用ECB解密，三步走，不说了
     *
     * @param decodedText
     * @param key
     * @return
     */
    public static String AesEcbDecode(byte[] decodedText, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(ECB_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(decodedText));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | IllegalBlockSizeException
                | BadPaddingException e) {
            logger.error("", e);
        }
        return null;

    }

    /**
     * CBC加密，三步走，只是在初始化时加了一个初始变量
     *
     * @param plainText
     * @param key
     * @param IVParameter
     * @return
     */
    public static byte[] AesCbcEncode(byte[] plainText, SecretKey key,
                                      byte[] IVParameter) {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(IVParameter);

            Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
            return cipher.doFinal(plainText);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException e) {
            logger.error("", e);
        }
        return null;
    }

    /**
     * CBC 解密
     *
     * @param decodedText
     * @param key
     * @param IVParameter
     * @return
     */
    public static String AesCbcDecode(byte[] decodedText, SecretKey key,
                                      byte[] IVParameter) {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(IVParameter);

        try {
            Cipher cipher = Cipher.getInstance(CBC_CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
            return new String(cipher.doFinal(decodedText));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException
                | IllegalBlockSizeException | BadPaddingException e) {
            logger.error("", e);
        }

        return null;

    }

    /**
     * 1.创建一个KeyGenerator 2.调用KeyGenerator.generateKey方法
     * 由于某些原因，这里只能是128，如果设置为256会报异常，原因在下面文字说明
     *
     * @return
     */
    public static byte[] generateAESSecretKey() {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            // keyGenerator.init(256);
            return keyGenerator.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            logger.error("", e);
        }
        return null;
    }

    /**
     * 还原密钥
     *
     * @param secretBytes
     * @return
     */
    public static SecretKey restoreSecretKey(byte[] secretBytes) {
        SecretKey secretKey = new SecretKeySpec(secretBytes, KEY_ALGORITHM);
        return secretKey;
    }
}

