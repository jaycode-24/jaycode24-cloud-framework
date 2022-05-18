package com.jaycode.framework.cloud.boot.core.support.encryptor;

public interface StringEncoder {
    /**
     * 加密
     *
     * @param data 明文
     * @return 密文
     */
    String encode(String data);

    /**
     * 解密
     *
     * @param data 密文
     * @return 明文
     */
    String decode(String data);
}
