package com.jaycode.framework.cloud.boot.core.config;

import cn.hutool.core.util.StrUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.jaycode.framework.cloud.boot.core.CloudApplicationBootException;
import com.jaycode.framework.cloud.boot.core.support.RuntimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author cheng.wang
 * @date 2022/5/13
 */
@Slf4j
public class ConfigServiceFactory {
    public static ConfigService getConfigService(BootstrapEnvironment bootstrapEnvironment) throws NacosException {
        Boolean standalone = bootstrapEnvironment.getBoolean("jaycode.standalone");
        if (Boolean.TRUE.equals(standalone)){
            log.info("启动单机模式");
            return new LocalYamlConfig();
        }else {
            System.setProperty("funi.standalone","false");
        }
        //读取配置中心配置
        String configServer = readConfigServer(bootstrapEnvironment);
        //如果存在ns参数则读取该命名空间
        if (configServer.contains("?ns=")){
            String[] parts = configServer.split("\\?ns=");
            configServer = parts[0];
            String namespace = parts[1];
            if (StrUtil.isNotBlank(namespace)){
                log.info("启用多租户模式，当前配置中心命名空间为：" + namespace);
                System.setProperty("acm.namespace", namespace);
                System.setProperty("spring.cloud.nacos.discovery.namespace", namespace);

            }

        }
        //转换为nacos配置
        System.setProperty("spring.cloud.nacos.config.server-addr", configServer);
        System.setProperty("spring.cloud.nacos.discovery.server-addr", configServer);
        System.setProperty("com.alibaba.nacos.client.naming.ctimeout", "30000");
        ConfigService configService = NacosFactory.createConfigService(configServer);
        log.info("完成配置中心:" + configServer + "初始化");
        return configService;
    }

    /**
     * 配置中心地址加载，按照以下顺序加载：
     * 1 程序运行参数 -Dxxx
     * 2 操作系统环境变量
     * 3 应用本身bootstrap.yaml
     * 4 如果检测到IDE模式，且配置了调试参数则采用调试参数
     *
     * @return 配置中心url地址
     */
    private static String readConfigServer(BootstrapEnvironment bootstrapEnvironment) {
        String configServer = bootstrapEnvironment.getString(ConfigConst.CONFIG_SERVER_DEF_KEY);
        //如果通过IDE 启动
        if (RuntimeUtils.isInIde()){
            log.info("当前应用通过IDE启动，相关调试配置已经生效");
            String debugConfigServer = bootstrapEnvironment.getString("funi.debug.config.server");
            if (StrUtil.isNotBlank(debugConfigServer)){
                configServer = debugConfigServer;
                log.info("读取调试信息[配置中心地址]:" + configServer);
            }
        }
        if (StrUtil.isEmpty(configServer)){
            throw new CloudApplicationBootException("bootstrap.yaml \r\n" + "--" + ConfigConst.CONFIG_SERVER_DEF_KEY + " 配置中心项未配置");

        }
        return configServer;
    }


    public static class LocalYamlConfig implements ConfigService{

        public LocalYamlConfig(){}

        @Override
        public String getConfig(String dataId, String group, long timeoutMs) throws NacosException {
            try {
                InputStream configData = ConfigServiceFactory.class.getClassLoader().getResourceAsStream("config/" + dataId);
                if (configData == null) {
                    log.warn("本地配置文件config/" + dataId + "未找到");
                    return null;
                }
                return IOUtils.toString(configData);
            } catch (IOException e) {
                log.warn("本地配置文件config/" + dataId + "未找到");
                return null;
            }
        }

        @Override
        public String getConfigAndSignListener(String dataId, String group, long timeoutMs, Listener listener) throws NacosException {
            try {
                return IOUtils.toString(ConfigServiceFactory.class.getResourceAsStream("config/" + dataId));
            } catch (IOException e) {
                throw new NacosException(-1, e);
            }
        }

        @Override
        public void addListener(String dataId, String group, Listener listener) throws NacosException {

        }

        @Override
        public boolean publishConfig(String dataId, String group, String content) throws NacosException {
            return false;
        }

        @Override
        public boolean removeConfig(String dataId, String group) throws NacosException {
            return false;
        }

        @Override
        public void removeListener(String dataId, String group, Listener listener) {

        }

        @Override
        public String getServerStatus() {
            return "UP";
        }
    }
}
