package com.jaycode.framework.cloud.boot.core.support;


import com.jaycode.framework.cloud.boot.core.support.encryptor.AsymmetricEncoder;
import com.jaycode.framework.cloud.boot.core.support.encryptor.GeneralCipherException;
import org.apache.commons.codec.StringEncoder;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * 密码工具类，提供常见加密解密算法
 *
 * @author jinlong.wang
 * @since 1.3.0
 */
public abstract class CipherUtils {
    public static StringEncoder DEFAULT_DES = getDesEncoder("4YztMHI7PsT4rLZN");

    /**
     * 常见对称加密算法
     */
    private enum SymmetricAlgorithm {
        AES, DES, SM4
    }

    /**
     * 获取DES加密器
     *
     * @param key 秘钥（长度为8的倍数）
     * @return 加密器
     */
    public static StringEncoder getDesEncoder(String key) {
        if (key.length() % 8 != 0) {
            throw new GeneralCipherException("DES密钥长度必须是8的倍数");
        }
        return new SimpleEncoder(key, SymmetricAlgorithm.DES.name());
    }

    /**
     * 获取AES加密器
     *
     * @param key 秘钥
     * @return 加密器
     */
    public static StringEncoder getAesEncoder(String key) {
        return new SimpleEncoder(key, SymmetricAlgorithm.AES.name());
    }


    /**
     * 获取SM4加密器
     *
     * @param key 秘钥,长度为16字节
     * @return 加密器
     */
    public static StringEncoder getSm4Encoder(String key) {
        if (!StringUtils.hasText(key) || key.length() != 16) {
            throw new GeneralCipherException("SM4密钥长度必须16字节");
        }
        return new Sm4Encoder(key);
    }
    /**
     * 获取SM4加密器
     *
     * @param keyBytes 秘钥字节数组
     * @return 加密器
     */
    public static StringEncoder getSm4Encoder(byte[] keyBytes) {
        return new Sm4Encoder(keyBytes);
    }
    /**
     * 获取SM2加密器,密钥都为16进制字符串
     *
     * @param publicKey 格式为 04 + X || Y 16进制字符串公钥 公钥长度为64字节,16进制编码后变为128字节,再加前缀04总共130字节
     * @param privateKey 16进制字符串私钥 私钥32字节,16进制编码后64字节
     * @return 加密器
     */
    public static AsymmetricEncoder getSm2Encoder(String publicKey, String privateKey) {
        if (!StringUtils.hasText(publicKey) || publicKey.length() != 130) {
            throw new GeneralCipherException("SM2的16进制公钥长度必须为130字节");
        }
        if (!StringUtils.hasText(privateKey) || privateKey.length() != 64) {
            throw new GeneralCipherException("SM2的16进制私钥长度必须为64字节");
        }
        return new Sm2Encoder(privateKey,publicKey);
    }

    /**
     * 简单对称加密器
     */
    private static class SimpleEncoder implements StringEncoder {
        private String key;
        private String algorithmName;
        private String padding;

        private SimpleEncoder(String key, String algorithmName, String padding) {
            this.key = key;
            this.algorithmName = algorithmName;
            this.padding = padding;
        }

        private SimpleEncoder(String key, String algorithmName) {
            this.key = key;
            this.algorithmName = algorithmName;
            this.padding = algorithmName + "/ECB/PKCS5Padding";
        }


        public String decode(String src) {
            String decryptStr = "";
            try {
                SecureRandom sr = new SecureRandom();
                DESKeySpec dks = new DESKeySpec(key.getBytes());
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithmName);
                SecretKey secretKey = keyFactory.generateSecret(dks);
                Cipher cipher = Cipher.getInstance(padding);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, sr);
                decryptStr = new String(cipher.doFinal(DigestUtils.hex2byte(src.getBytes())), DigestUtils.DEFAULT_CHARSET);
            } catch (Exception ex) {
                throw new GeneralCipherException("解密失败" + ex.getMessage(), ex);
            }
            return decryptStr;
        }

        public String encode(String src) {
            byte[] bytes = null;
            String encryptStr = "";
            try {
                SecureRandom sr = new SecureRandom();
                DESKeySpec dks = new DESKeySpec(key.getBytes());
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(algorithmName);
                SecretKey secretKey = keyFactory.generateSecret(dks);
                Cipher cipher = Cipher.getInstance(padding);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, sr);
                bytes = cipher.doFinal(src.getBytes(DigestUtils.DEFAULT_CHARSET));

            } catch (Exception ex) {
                throw new GeneralCipherException("加密失败" + ex.getMessage(), ex);
            }
            if (bytes != null) {
                encryptStr = DigestUtils.byte2hex(bytes);
            }
            return encryptStr;
        }


    }

    /**
     * sm4对称加密器
     */
    private static class Sm4Encoder implements StringEncoder {
        private byte[] key;

        private Sm4Encoder(String key) {
            this.key = StringUtils.getBytesUtf8(key);
        }
        private Sm4Encoder(byte[] keyBytes) {
            this.key = keyBytes;
        }

        public String decode(String src) {
            try {
                Sm4Context ctx = new Sm4Context();
                ctx.isPadding = true;
                ctx.mode = Sm4.SM4_DECRYPT;

                Sm4 sm4 = new Sm4();
                sm4.sm4SetKeyDec(ctx, this.key);
                //先16进制解码,再解密
                byte[] decrypted = sm4.sm4CryptEcb(ctx, DigestUtils.hex2byte(src));
                return new String(decrypted, StandardCharsets.UTF_8);
            } catch (Exception ex) {
                throw new GeneralCipherException("SM4解密失败" + ex.getMessage(), ex);
            }
        }

        public String encode(String src) {
            try {
                Sm4Context ctx = new Sm4Context();
                ctx.isPadding = true;
                ctx.mode = Sm4.SM4_ENCRYPT;

                Sm4 sm4 = new Sm4();
                sm4.sm4SetKeyEnc(ctx, key);
                byte[] encrypted = sm4.sm4CryptEcb(ctx, StringUtils.getBytesUtf8(src));
                //加密结果转换为16进制字符串,便于保存
                return DigestUtils.byte2hex(encrypted);

            } catch (Exception ex) {
                throw new GeneralCipherException("SM4加密失败" + ex.getMessage(), ex);
            }
        }
    }
    /**
     * SM2非对称加密器
     */
    private static class Sm2Encoder implements AsymmetricEncoder {
        //私钥16进制字符串
        private String privateKey;
        //公钥16进制字符串
        private String publicKey;
        private Sm2Encoder(String privateKey,String publicKey) {
            this.privateKey = privateKey;
            this.publicKey = publicKey;
        }

        @Override
        public String encode(String data) {
            if(!StringUtils.hasText(publicKey)) {
                throw new GeneralCipherException("加密公钥不能为空");
            }
            try {
                return Sm2Utils.encrypt(publicKey,data);
            } catch (Exception ex) {
                throw new GeneralCipherException("Sm2加密失败" + ex.getMessage(), ex);
            }
        }

        @Override
        public String decode(String data) {
            if(!StringUtils.hasText(privateKey)) {
                throw new GeneralCipherException("解密私钥不能为空");
            }
            try {
                return Sm2Utils.decrypt(privateKey,data);
            } catch (Exception ex) {
                throw new GeneralCipherException("Sm2解密失败" + ex.getMessage(), ex);
            }
        }

        @Override
        public boolean verifySign(String sourceData, String signData) {
            if(!StringUtils.hasText(publicKey)) {
                throw new GeneralCipherException("验签公钥不能为空");
            }
            try {
                byte[] signByte = DigestUtils.hex2byte(signData);
                return Sm2Utils.verifySign(DigestUtils.hex2byte(publicKey),
                        StringUtils.getBytesUtf8(sourceData),
                        signByte);
            } catch (Exception ex) {
                throw new GeneralCipherException("Sm2验签失败" + ex.getMessage(), ex);
            }
        }

        @Override
        public String sign(String sourceData) {
            if(!StringUtils.hasText(privateKey)) {
                throw new GeneralCipherException("签名私钥不能为空");
            }
            try {
                //签名完成数组
                byte[] sign = Sm2Utils.sign(DigestUtils.hex2byte(privateKey), StringUtils.getBytesUtf8(sourceData));
                //签名数组指转为字符串
                return DigestUtils.byte2hex(sign);
            } catch (Exception ex) {
                throw new GeneralCipherException("Sm2签名失败" + ex.getMessage(), ex);
            }
        }
    }
}
