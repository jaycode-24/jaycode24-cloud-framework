package com.jaycode.framework.cloud.boot.core.config;

import cn.hutool.core.util.StrUtil;
import com.jaycode.framework.cloud.boot.core.CloudApplicationBootException;
import com.jaycode.framework.cloud.boot.core.JayCodeApplication;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.cache.annotation.Cacheable;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置工厂类
 *  * 产生配置读取对象
 *  * 提供本地启动类配置读取以及远程配置中心配置信息读取
 *  * 配置分为两类
 *  * 1 bootstrap.yaml 启动依赖类，必须静态本地配置
 *  * 2 应用名.yaml 应用程序运行类，支持本地配置以及配置中心配置，一般为了方便配置管理，通常把应用名.yaml配置放在配置中心配置
 *  *
 * @author cheng.wang
 * @date 2022/5/13
 */
@Slf4j
public class ConfigFactory {
    private static final String DEFAULT_BOOTSTRAP_FILE_NAME = "bootstrap.yaml";
    //静态变量优于构造方法执行（通过静态变量初始化本类）
    private static final ConfigFactory INSTANCE = new ConfigFactory();
    private final Map<String, Config> configCache = new HashMap<>();
    //配置中心连接超时为5分钟
    private static final int CONFIG_CONNECT_TIMEOUT = 300;
    private static final int CC_TIMEOUT = CONFIG_CONNECT_TIMEOUT * 1000;

    private final YamlReader bootstrapYaml;

    private ConfigFactory(){
        try {
            //先获取本地配置文件
            InputStream bootstrapConfig = JayCodeApplication.class.getClassLoader().getResourceAsStream(DEFAULT_BOOTSTRAP_FILE_NAME);
            if (bootstrapConfig == null || bootstrapConfig.available() == 0){
                //本地配置文件必须存在
                throw new CloudApplicationBootException("bootstrap.yaml 未配置或内容为空");
            }
            //创建启动配置类
            bootstrapYaml = new YamlReader(IOUtils.toString(bootstrapConfig));
        }catch (Exception e){
            throw new CloudApplicationBootException(e.getMessage(),e);
        }
    }

    /**
     * 获取bootstrap.yaml配置
     * @return
     */
    public static Config getBootstrapConfig() {
        return new YamlConfig(INSTANCE.bootstrapYaml);
    }

    /**
     * 获取common.yaml配置
     * @return
     */
    public static Config getShareConfig() {
        return getConfig(ConfigConst.SHARE_DATA_ID);
    }

    public static Config getConfig(String configId) {
        return getConfig(configId,true);
    }

    public static Config getConfig(String configId, boolean cache) {
        if (cache){
            //标明这个配置有缓存
            if (!INSTANCE.configCache.containsKey(configId)){
                //但是从缓存中没取到，就重新去配置中心读
                Config config = createConfigReader(configId);
                if (config instanceof NotFoundConfig){
                    return config;
                }
                INSTANCE.configCache.put(configId,config);
            }
            return INSTANCE.configCache.get(configId);
        }else {
            return createConfigReader(configId);
        }

    }

    /**
     * 从配置中心读取最新的配置文件
     *
     * @param configId 配置ID
     * @return 配置文件内容
     */
    private static Config createConfigReader(String configId) {
        return Try.of(() -> {
            String appConfigYaml = ConfigServiceHolder.getConfigService().getConfig(configId, null, CC_TIMEOUT);
            if (StrUtil.isEmpty(appConfigYaml)){
                log.warn("配置中心返回" + configId + "内容为空");
                return new NotFoundConfig(configId);
            }
            return new YamlConfig(new YamlReader(appConfigYaml),configId,ConfigServiceHolder.getConfigService());
        }).getOrElseGet(e -> {
           //log.error(e);
           return new NotFoundConfig(configId);
        });
    }
    /**
     * 获取配置中心配置的应用服务配置
     *
     * @return 配置中心应用配置
     */
    public static Config getAppConfig() {
        String applicationName = ConfigConst.getApplicationName();
        return getConfig(applicationName + ".yaml");
    }


    public static class NotFoundConfig implements Config{

        private final String configId;

        public NotFoundConfig(String configId) {
            this.configId = configId;
        }

        @Override
        public boolean has(String key) {
            return false;
        }

        @Override
        public <T> T getValue(String key) {
            throw new ConfigSourceNotFoundException("配置中心配置文件:" + configId + "未找到或内容为空");
        }

        @Override
        public <T> T getValue(String key, T defaultValue) {
            throw new ConfigSourceNotFoundException("配置中心配置文件:" + configId + "未找到或内容为空");
        }

        @Override
        public Boolean getBoolean(String key) {
            throw new ConfigSourceNotFoundException("配置中心配置文件:" + configId + "未找到或内容为空");
        }

        @Override
        public String asString() {
            return "";
        }

        @Override
        public void addListener(ConfigListener configListener) {

        }
    }
}
