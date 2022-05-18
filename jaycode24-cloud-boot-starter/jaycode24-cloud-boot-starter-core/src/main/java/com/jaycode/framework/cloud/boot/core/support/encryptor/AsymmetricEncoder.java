package com.jaycode.framework.cloud.boot.core.support.encryptor;
/**
 * 非对称加密接口
 *
 * @author chen.chen
 */
public interface AsymmetricEncoder extends StringEncoder{
    /**
     * 签名验证
     *
	 * @param sourceData 待签名原文
	 * @param signData 默认认为签名是16进制字符串
     * @return 签名验证结果,true验签成功,false验签失败
     */
    boolean verifySign(String sourceData, String signData);
    /**
     * 获取签名,16进制字符串
     *
     * @param sourceData 待签名原文
     * @return 签名结果字符串
     */
    String sign(String sourceData);
}
