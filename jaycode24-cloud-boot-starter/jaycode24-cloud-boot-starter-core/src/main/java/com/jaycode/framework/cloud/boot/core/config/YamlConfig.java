package com.jaycode.framework.cloud.boot.core.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.jaycode.framework.cloud.boot.core.CloudFrameworkException;

import java.util.concurrent.Executor;

/**
 * @author cheng.wang
 * @date 2022/5/13
 */
@SuppressWarnings("unchecked")
public class YamlConfig implements Config{

    private final YamlReader yamlReader;
    private String id;
    private ConfigService configService;

    public YamlConfig(YamlReader yamlReader, String id, ConfigService configService) {
        this.yamlReader = yamlReader;
        this.id = id;
        this.configService = configService;
    }

    public YamlConfig(YamlReader yamlReader) {
        this.yamlReader = yamlReader;
    }

    @Override
    public boolean has(String key) {
        return false;
    }

    @Override
    public <T> T getValue(String key) {
        return (T)yamlReader.get(key);
    }

    @Override
    public <T> T getValue(String key, T defaultValue) {
        T v = getValue(key);
        if (v == null) {
            return defaultValue;
        }
        return v;
    }

    @Override
    public Boolean getBoolean(String key) {
        Boolean b = getValue(key);
        if (b != null) {
            return b;
        }
        return false;
    }

    @Override
    public String asString() {
        return null;
    }

    @Override
    public void addListener(ConfigListener configListener) {
        if (configService != null && StrUtil.isNotEmpty(id)){
            try {
                configService.addListener(id,null,new NacosConfigListenerAdaptor(configListener));
            }catch (NacosException e){
                throw new CloudFrameworkException("配置监听器注册失败" + e.getErrMsg(), e);
            }
        }
    }

    private class NacosConfigListenerAdaptor implements Listener{

        private ConfigListener configListener;

        public NacosConfigListenerAdaptor(ConfigListener configListener) {
            this.configListener = configListener;
        }

        @Override
        public Executor getExecutor() {
            return null;
        }

        @Override
        public void receiveConfigInfo(String configInfo) {
            configListener.receiveConfigInfo(new YamlConfig(new YamlReader(configInfo),id,configService));
        }
    }
}
