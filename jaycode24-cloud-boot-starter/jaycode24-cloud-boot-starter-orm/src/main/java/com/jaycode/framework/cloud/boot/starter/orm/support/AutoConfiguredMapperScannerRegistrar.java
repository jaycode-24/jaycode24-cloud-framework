package com.jaycode.framework.cloud.boot.starter.orm.support;

import com.jaycode.framework.cloud.boot.starter.orm.OrmAutoConfiguration;
import com.jaycode.framework.cloud.boot.starter.orm.ds.annotation.Repository;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * Mybatis Bean 扫描类
 *  * 提供给{@link OrmAutoConfiguration}完成mybatis相关bean扫描
 * @author cheng.wang
 * @date 2022/5/16
 */
@Slf4j
public class AutoConfiguredMapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {
    private ResourceLoader resourceLoader;

    /**
     * 加载自定义的bean
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        log.info("启动ORM实体仓库Bean 扫描");
        log.info("---扫描规则1:父包必须符合com.funi.cloud.**.[domain|module].**." + Repository.class.getSimpleName().toLowerCase() + "规则");
        log.info("---扫描规则2:接口实现" + Repository.class.getName() + "注解");
        log.info("---扫描规则3:mapper配置路径在classpath*:/repository/**/*" + Repository.class.getSimpleName().toLowerCase() + ".xml下");
        //创建扫描器
        ClassPathMapperScanner scanner = new CustomClassPathMapperScanner(registry);
        try {
            if (this.resourceLoader != null){
                scanner.setResourceLoader(this.resourceLoader);
            }
            scanner.registerFilters();
            scanner.doScan("com.jaycode.cloud.**.domain.**.repository", "com.funi.cloud.**.module.**.repository");
        }catch (IllegalStateException var7) {
            log.debug( "Could not determine auto-configuration package, automatic mapper scanning disabled.");
        }
    }

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
