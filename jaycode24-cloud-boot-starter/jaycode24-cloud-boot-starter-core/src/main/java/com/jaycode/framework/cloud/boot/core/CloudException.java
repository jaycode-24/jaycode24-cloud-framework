package com.jaycode.framework.cloud.boot.core;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author cheng.wang
 * @date 2022/5/13
 */
public class CloudException extends RuntimeException{
    private static final long serialVersionUID = -1398349861724467675L;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 附加属性
     */
    private Map<String,Object> attributes = new HashMap<>();

    public CloudException(){

    }

    public CloudException(String message){
        super(message);
    }

    public CloudException(String message,Throwable throwable){
        super(message,throwable);
    }

    public String getErrorCode(){
        return this.errorCode;
    }

    /**
     * 设置错误码，参照{@link this#setErrorCode(Integer)}
     *
     * @param code 错误码编码
     * @see this#setErrorCode(Integer)
     */
    @Deprecated
    public void setCode(Integer code) {
        setErrorCode(String.valueOf(code));
    }
    /**
     * 设置字符类型错误码
     *
     * @param errorCode 错误码
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * 设置整型错误码
     *
     * @param errorCode 错误码
     */
    public void setErrorCode(Integer errorCode) {
        setErrorCode(String.valueOf(errorCode));
    }

    /**
     * 获取异常附加属性信息
     *
     * @return 附加属性信息
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * 设置附加属性信息
     *
     * @param attributes Map属性信息
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    /**
     * 设置KV属性信息
     *
     * @param key   键名
     * @param value 值
     */
    public void setAttribute(String key, String value) {
        this.attributes.put(key, value);
    }
    /**
     * 链式设置附加属性
     *
     * @param action 消费者函数
     * @param <T>    原始异常类
     * @return 原始异常对象
     */
    public <T extends CloudException> T attributes(Consumer<Map<String, Object>> action) {
        this.attributes = new HashMap<>();
        action.accept(this.attributes);
        return (T) this;
    }

    /**
     * 链式设置附加属性
     *
     * @param attributes Map对象
     * @param <T>        原始异常类
     * @return 原始异常对象
     */
    public <T extends CloudException> T attributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return (T) this;
    }

    /**
     * 链式设置附加属性
     *
     * @param key   键名
     * @param value 值
     * @param <T>   原始异常类
     * @return 原始异常对象
     */
    public <T extends CloudException> T attribute(String key, Object value) {
        this.attributes.put(key, value);
        return (T) this;
    }

    /**
     * 链式设置错误码
     *
     * @param errorCode 错误码
     * @param <T>       原始异常类型
     * @return 原始异常对象
     * @see this#errorCode(String)
     * @deprecated 方法签名定义不合法
     */
    @Deprecated
    public <T extends CloudException> T code(String errorCode) {
        setErrorCode(errorCode);
        return (T) this;
    }

    /**
     * 链式设置错误码
     *
     * @param errorCode 错误码
     * @param <T>       原始异常类型
     * @return 原始异常对象
     */
    public <T extends CloudException> T errorCode(String errorCode) {
        setErrorCode(errorCode);
        return (T) this;
    }
}
