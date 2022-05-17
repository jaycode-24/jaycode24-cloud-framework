package com.jaycode.framework.cloud.boot.core.config;

import com.alibaba.nacos.api.config.ConfigService;
import com.jaycode.framework.cloud.boot.core.CloudApplicationBootException;

/**
 * @author cheng.wang
 * @date 2022/5/13
 */
public class ConfigServiceHolder {

    private static ConfigService CS;

    private ConfigServiceHolder(){

    }

    public static void initConfigService() {
        //默认参数
        //启用微服务配置中心
        System.setProperty("spring.cloud.config.enabled", "false");
        //设置公共配置文件id
        System.setProperty("spring.cloud.nacos.config.shared-dataids", "common.yaml");
        //允许bean 定义重写
        System.setProperty("spring.main.allow-bean-definition-overriding", "true");
        //配置中心默认后缀
        System.setProperty("spring.cloud.nacos.config.file-extension", "yaml");
        //允许文件上传
        System.setProperty("spring.servlet.multipart.enabled", "true");
        try {
            CS = ConfigServiceFactory.getConfigService(new BootstrapEnvironment());
        }catch (Exception e){
            throw new CloudApplicationBootException(e.getMessage(), e);
        }
    }

    public static ConfigService getConfigService() {
        return CS;
    }
}
