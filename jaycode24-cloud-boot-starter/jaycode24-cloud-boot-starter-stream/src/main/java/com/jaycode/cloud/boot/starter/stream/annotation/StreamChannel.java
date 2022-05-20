package com.jaycode.cloud.boot.starter.stream.annotation;

import java.lang.annotation.*;

/**
 * @author cheng.wang
 * @date 2022/5/19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface StreamChannel {

    String value() default "";

}
