package com.jaycode.framework.cloud.boot.core.config;

import cn.hutool.core.util.StrUtil;

/**
 * 启动环境读取规则
 *  * 1 统属性表
 *  * 2 环境变量
 *  * 3 配置文件
 *  * 其次bootstrap.yaml
 * @author cheng.wang
 * @date 2022/5/13
 */
public class BootstrapEnvironment {

    private Config config;

    public BootstrapEnvironment (){
        this.config = ConfigFactory.getBootstrapConfig();
    }


    /**
     * 通过key获取值
     * @param key
     * @return
     */
    public String getString(String key) {
        //先获取环境变量，优先
        String m = getEnv(key);
        //再从配置中获取
        return StrUtil.isNotBlank(m) ? m : config.getValue(key);
    }

    /**
     * 获取非配置变量
     * @param key
     * @return
     */
    private String getEnv(String key) {
        String env = System.getProperty(key);
        if (StrUtil.isBlank(env)){
            env = System.getenv(key.toUpperCase().replace(".","_"));
        }
        return env;
    }

    public Boolean getBoolean(String key){
        String m = getEnv(key);
        if (StrUtil.isNotBlank(m)){
            if ("true".equals(m)) {
                return true;
            }
            if ("false".equals(m)) {
                return false;
            }
        }
        return config.getBoolean(key);
    }
}
