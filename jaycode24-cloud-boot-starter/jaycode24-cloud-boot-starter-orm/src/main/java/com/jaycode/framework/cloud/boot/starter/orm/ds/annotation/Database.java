package com.jaycode.framework.cloud.boot.starter.orm.ds.annotation;

import java.lang.annotation.*;

/**
 * @author cheng.wang
 * @date 2022/5/16
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Database {

    String value();
}
