package com.jaycode.framework.cloud.boot.starter.orm.support;

import com.jaycode.framework.cloud.boot.starter.orm.ds.annotation.Repository;
import org.mybatis.spring.mapper.ClassPathMapperScanner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

/**
 * @author cheng.wang
 * @date 2022/5/16
 */
public class CustomClassPathMapperScanner extends ClassPathMapperScanner {
    public CustomClassPathMapperScanner(BeanDefinitionRegistry registry) {
        super(registry);
        setAnnotationClass(Repository.class);
    }
}
