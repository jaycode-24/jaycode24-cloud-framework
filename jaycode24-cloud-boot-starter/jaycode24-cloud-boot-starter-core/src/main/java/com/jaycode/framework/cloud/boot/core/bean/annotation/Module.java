package com.jaycode.framework.cloud.boot.core.bean.annotation;

import java.lang.annotation.*;

/**
 * @author cheng.wang
 * @date 2022/5/13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Module {

    /**
     * 模块名
     * @return 模块名
     */
    String value();
}
