package com.jaycode.framework.cloud.boot.starter.fileupload;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件组件自动配置主类
 * @author cheng.wang
 * @date 2022/5/19
 */
@Configuration
public class FileUploadAutoConfiguration {

    @Bean
    public FileUploader fileUploader(StorageClient storageClient) {
        return new FileUploader(storageClient);
    }

    @ConditionalOnMissingBean(StorageClient.class)
    @Bean(initMethod = "init", destroyMethod = "destroy")
    public StorageClient storageClient(LoadBalancerClient loadBalancerClient) {
        return new DefaultStorageClient(loadBalancerClient);
    }
}
