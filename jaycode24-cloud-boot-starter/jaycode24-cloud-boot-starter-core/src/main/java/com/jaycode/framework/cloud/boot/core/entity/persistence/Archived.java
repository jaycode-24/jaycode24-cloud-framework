package com.jaycode.framework.cloud.boot.core.entity.persistence;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 归档注解，在{@link com.jaycode.framework.cloud.boot.starter.orm.entity.BaseRepository#archive}时候生效
 * 传统归档操作都是将逻辑删除标志置为true|false，但是有时候需要在归档的时候同步更新其他字段，比如删除时间、删除人ID等
 * 所以，引入了primary属性，primary为true应该加载逻辑字段上，其他需要同步更新的字段primary保持默认即可(false)
 *
 * @author jinlong.wang
 */
@Target({FIELD})
@Retention(RUNTIME)
public @interface Archived {
    /**
     * 是否是逻辑删除控制字段
     *
     * @return true 是
     */
    boolean primary() default false;
}
