package com.jaycode.framework.cloud.boot.core.support.connection;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 数据库连接帮助类
 * @author cheng.wang
 * @date 2022/5/18
 */
public abstract class ConnectionUtils {
    public static final String PASSWORD_VAR = "__PASSWORD__";
    public static final String USERNAME_VAR = "__USERNAME__";
    //Key必须保持16位
    private static final String KEY = "3132577a78622364396566324d4d2424";
    private static final String AES = "AES";
    private static final String AES_ECB_PKCS5PADDING = "AES/ECB/PKCS5Padding";
    private static final SecretKeySpec SECRET_KEY_SPEC;


    static {
        byte[] raw = DigestUtils.hex2byte(KEY.getBytes());
        SECRET_KEY_SPEC = new SecretKeySpec(raw, AES);
    }

    /**
     * 对数据库密码解密
     *
     * @param sSrc 密码密文
     * @return 原密码
     */
    public static String decode(String sSrc) {
        try {

            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5PADDING);
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY_SPEC);
            //先用base64解密
            byte[] encrypted1 = DigestUtils.base64Decode(sSrc);
            byte[] original = cipher.doFinal(encrypted1);
            return new String(original, DigestUtils.DEFAULT_CHARSET);
        } catch (Exception ex) {
            throw new ConnectionAuthException("连接认证失败，" + ex.getMessage(), ex);
        }
    }

    public static String encode(String sSrc) {
        try {
            Cipher cipher = Cipher.getInstance(AES_ECB_PKCS5PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY_SPEC);
            byte[] original = cipher.doFinal(sSrc.getBytes(DigestUtils.DEFAULT_CHARSET));
            return DigestUtils.base64Encode(original);
        } catch (Exception ex) {
            throw new ConnectionAuthException("连接认证失败，" + ex.getMessage(), ex);
        }
    }
}
