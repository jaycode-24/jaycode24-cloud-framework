package com.jaycode.framework.cloud.boot.starter.orm.ds.annotation;

import java.lang.annotation.*;

/**
 * 持久层注解
 * @author cheng.wang
 * @date 2022/5/16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@org.springframework.stereotype.Repository
public @interface Repository {

    String route() default "";

    boolean dialect() default false;
}
