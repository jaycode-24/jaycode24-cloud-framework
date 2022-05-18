package com.jaycode.framework.cloud.boot.core.support;


import com.jaycode.framework.cloud.boot.core.CloudFrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.StringEncoder;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 编码类，提供常见的消息编码算法，包括md5、hex、base64、url等
 *
 * @author jinlong.wang
 */
@Slf4j
public abstract class DigestUtils {
    public static final String DEFAULT_CHARSET = "utf-8";

    /**
     * 获取DES加密器
     *
     * @param key 秘钥（长度为8的倍数）
     * @return 加密器
     * @deprecated 将从1.3.1 版本中删除
     */
    @Deprecated
    public static StringEncoder getDesEncoder(String key) {
        if (key.length() % 8 != 0) {
            throw new CloudFrameworkException("DES密钥长度必须是8的倍数");
        }
        return CipherUtils.getDesEncoder(key);
    }

    /**
     * 整形转换成网络传输的字节流（字节数组）型数据
     *
     * @param num 一个整型数据
     * @return 4个字节的自己数组
     */
    public static byte[] intToBytes(int num) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (0xff & (num >> 0));
        bytes[1] = (byte) (0xff & (num >> 8));
        bytes[2] = (byte) (0xff & (num >> 16));
        bytes[3] = (byte) (0xff & (num >> 24));
        return bytes;
    }

    /**
     * 长整形转换成网络传输的字节流（字节数组）型数据
     *
     * @param num 一个长整型数据
     * @return 4个字节的自己数组
     */
    public static byte[] longToBytes(long num) {
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (0xff & (num >> (i * 8)));
        }

        return bytes;
    }


    /**
     * 四个字节的字节数据转换成一个整形数据
     *
     * @param bytes 4个字节的字节数组
     * @return 一个整型数据
     */
    public static int byteToInt(byte[] bytes) {
        int num = 0;
        int temp;
        temp = (0x000000ff & (bytes[0])) << 0;
        num = num | temp;
        temp = (0x000000ff & (bytes[1])) << 8;
        num = num | temp;
        temp = (0x000000ff & (bytes[2])) << 16;
        num = num | temp;
        temp = (0x000000ff & (bytes[3])) << 24;
        num = num | temp;
        return num;
    }

    /**
     * 将字节数组转换为字符串
     *
     * @param bytes 字节数组
     * @return 字符串
     */
    public static String byte2hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (final byte b : bytes) {
            String s = Integer.toHexString(0xff & b);

            if (s.length() < 2) {
                sb.append("0");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 将16进制字符串对应字节数组还原为字节数组
     *
     * @param b 16进制数组
     * @return 原文字节数组
     */
    public static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0)
            throw new IllegalArgumentException("length not even");
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        return b2;
    }

    /**
     * 将16进制字符串还原为字节数组
     *
     * @param src 16进制字符
     * @return 原文字节数组
     */
    public static byte[] hex2byte(String src) {
        return hex2byte(src.getBytes());
    }

    /**
     * URL 编码
     *
     * @param url 原文
     * @return 编码后字符
     */
    public static String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new CloudFrameworkException(e.getMessage(), e);
        }
    }

    /**
     * URL 解码
     *
     * @param url 编码后字符
     * @return 原文
     */
    public static String urlDecode(String url) {
        try {
            return URLDecoder.decode(url, DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new CloudFrameworkException(e.getMessage(), e);
        }
    }

    /**
     * base64 编码（RFC4648）
     *
     * @param bytes 原文字节数组
     * @return 字符
     */
    public static String base64Encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * base64 解码（RFC4648）
     *
     * @param text 编码后字符
     * @return 原文字节数组
     */
    public static byte[] base64Decode(String text) {
        return Base64.getDecoder().decode(text.getBytes());
    }

    /**
     * 提供MD5 摘要计算，返回值为16进制字符串
     *
     * @param src 原文字符串
     * @return 摘要
     */
    public static String md5Hex(String src) {
        return md5Hex(src.getBytes());
    }

    /**
     * 提供MD5 摘要计算，返回值为16进制字符串
     *
     * @param src 原文字节数组
     * @return 摘要
     */
    public static String md5Hex(byte[] src) {
        try {
            // 加密对象，指定加密方式
            MessageDigest md5 = MessageDigest.getInstance("md5");
            // 加密
            byte[] digest = md5.digest(src);
            // 十六进制的字符
            return byte2hex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new CloudFrameworkException(e.getMessage(), e);
        }
    }

    /**
     * 提供SM3 摘要计算，返回值为16进制字符串
     *
     * @param src 原文字符串
     * @return 摘要
     */
    public static String sm3Hex(String src) {
        return sm3Hex(StringUtils.getBytesUtf8(src));
    }
    /**
     * sm3摘要计算,返回值为16进制字符
     *
     * @param src   原文字节数组
     * @return 摘要
     */
    public static String sm3Hex(byte[] src) {
        //摘要计算后固定长度32字节
        byte[] md = new byte[32];
        //创建摘要算法类
        Sm3Digest sm3 = new Sm3Digest();
        //明文刷入缓冲区
        sm3.update(src, 0, src.length);
        //密文刷出缓冲区
        sm3.doFinal(md, 0);
        return DigestUtils.byte2hex(md);
    }

    /**
     * 基于sha1计算输入内容消息摘要
     *
     * @param data 输入内容
     * @return 摘要字节数组
     * @throws IOException 流处理异常
     */
    public static byte[] sha1(InputStream data) throws IOException {
        return digest(getSha1Digest(), data);
    }

    /**
     * 基于sha1计算输入内容数组消息摘要
     *
     * @param data 输入内容
     * @return 摘要字节数组
     */
    public static byte[] sha1(byte[] data) {
        return getSha1Digest().digest(data);
    }

    /**
     * 基于sha1计算输入内容消息摘要
     *
     * @param data 输入内容
     * @return 摘要字节数组
     */
    public static byte[] sha1(String data) {
        return sha1(StringUtils.getBytesUtf8(data));
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static String sha1Hex(byte[] data) {
        return byte2hex(sha1(data));
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static String sha1Hex(InputStream data) throws IOException {
        return byte2hex(sha1(data));
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static String sha1Hex(String data) {
        return byte2hex(sha1(data));
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static byte[] sha256(byte[] data) {
        return getSha256Digest().digest(data);
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     * @throws IOException 流处理异常
     */
    public static byte[] sha256(InputStream data) throws IOException {
        return digest(getSha256Digest(), data);
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static byte[] sha256(String data) {
        return sha256(StringUtils.getBytesUtf8(data));
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static String sha256Hex(byte[] data) {
        return byte2hex(sha256(data));
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static String sha256Hex(InputStream data) throws IOException {
        return byte2hex(sha256(data));
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static String sha256Hex(String data) {
        return byte2hex(sha256(data));
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static byte[] sha512(byte[] data) {
        return getSha512Digest().digest(data);
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     * @throws IOException 流处理异常
     */
    public static byte[] sha512(InputStream data) throws IOException {
        return digest(getSha512Digest(), data);
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static byte[] sha512(String data) {
        return sha512(StringUtils.getBytesUtf8(data));
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static String sha512Hex(byte[] data) {
        return byte2hex(sha512(data));
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static String sha512Hex(InputStream data) throws IOException {
        return byte2hex(sha512(data));
    }

    /**
     * 基于sha1计算输入内容消息摘要，并按16进制返回
     *
     * @param data 输入内容
     * @return 摘要字符串
     */
    public static String sha512Hex(String data) {
        return byte2hex(sha512(data));
    }

    /**
     * 创建HmacMD5 Key 字节数组
     *
     * @return Key 字节数组
     */
    public static byte[] getHmacMd5Key() {
        return getHmacKey("HmacMD5");
    }

    /**
     * 创建 HmacSHA256
     *
     * @return Key 字节数组
     */
    public static byte[] getHmacSha256Key() {
        return getHmacKey("HmacSHA256");
    }

    /**
     * 创建 HmacSHA512
     *
     * @return Key 字节数组
     */
    public static byte[] getHmacSha512Key() {
        return getHmacKey("HmacSHA512");
    }


    /**
     * HMAC MD5 加密
     *
     * @param data 原文
     * @param key  秘钥
     * @return 摘要
     */
    public static String hmacMd5(byte[] data, byte[] key) {
        HMac hmac = new HMac(new MD5Digest());
        hmac.init(new KeyParameter(key));
        hmac.update(data, 0, data.length);
        byte[] rsData = new byte[hmac.getMacSize()];
        hmac.doFinal(rsData, 0);
        return byte2hex(rsData);
    }

    /**
     * HMAC Sha256 摘要
     *
     * @param data 原文
     * @param key  秘钥
     * @return 摘要
     */
    public static String hmacSha256(byte[] data, byte[] key) {
        HMac hmac = new HMac(new SHA256Digest());
        hmac.init(new KeyParameter(key));
        hmac.update(data, 0, data.length);
        byte[] rsData = new byte[hmac.getMacSize()];
        hmac.doFinal(rsData, 0);
        return byte2hex(rsData);
    }

    /**
     * HMAC Sha512 摘要
     *
     * @param data 原文
     * @param key  秘钥
     * @return 摘要
     */
    public static String hmacSha512(byte[] data, byte[] key) {
        HMac hmac = new HMac(new SHA512Digest());
        hmac.init(new KeyParameter(key));
        hmac.update(data, 0, data.length);
        byte[] rsData = new byte[hmac.getMacSize()];
        hmac.doFinal(rsData, 0);
        return byte2hex(rsData);
    }

    // 获取 HMAC Key
    private static byte[] getHmacKey(String type) {
        try {
            // 1、创建密钥生成器
            KeyGenerator keyGenerator = KeyGenerator.getInstance(type);
            // 2、产生密钥
            SecretKey secretKey = keyGenerator.generateKey();
            // 3、获取密钥
            return secretKey.getEncoded();
        } catch (Exception e) {
            throw new CloudFrameworkException(e.getMessage(), e);
        }
    }

    /**
     * 获取摘要计算器
     *
     * @param algorithm 根据摘要算法
     * @return 摘要计算器
     */
    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException var2) {
            throw new IllegalArgumentException(var2);
        }
    }

    /**
     * 获取md5摘要计算器
     *
     * @return 摘要计算器
     */
    private static MessageDigest getMd5Digest() {
        return getDigest("MD5");
    }

    /**
     * 获取sha1摘要计算器
     *
     * @return 摘要计算器
     */
    private static MessageDigest getSha1Digest() {
        return getDigest("SHA-1");
    }

    /**
     * 获取sha256摘要计算器
     *
     * @return 摘要计算器
     */
    private static MessageDigest getSha256Digest() {
        return getDigest("SHA-256");
    }

    /**
     * 获取sha512摘要计算器
     *
     * @return 摘要计算器
     */
    private static MessageDigest getSha512Digest() {
        return getDigest("SHA-512");
    }


    private static byte[] digest(MessageDigest messageDigest, byte[] data) {
        return messageDigest.digest(data);
    }

    private static byte[] digest(MessageDigest messageDigest, ByteBuffer data) {
        messageDigest.update(data);
        return messageDigest.digest();
    }

    private static byte[] digest(MessageDigest messageDigest, File data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    private static byte[] digest(MessageDigest messageDigest, InputStream data) throws IOException {
        return updateDigest(messageDigest, data).digest();
    }

    private static MessageDigest updateDigest(MessageDigest digest, File data) throws IOException {
        BufferedInputStream stream = new BufferedInputStream(new FileInputStream(data));

        MessageDigest var3;
        try {
            var3 = updateDigest(digest, (InputStream) stream);
        } finally {
            stream.close();
        }

        return var3;
    }

    private static MessageDigest updateDigest(MessageDigest digest, InputStream data) throws IOException {
        byte[] buffer = new byte[1024];

        for (int read = data.read(buffer, 0, 1024); read > -1; read = data.read(buffer, 0, 1024)) {
            digest.update(buffer, 0, read);
        }

        return digest;
    }

    private static MessageDigest updateDigest(MessageDigest messageDigest, String valueToDigest) {
        messageDigest.update(StringUtils.getBytesUtf8(valueToDigest));
        return messageDigest;
    }


}
