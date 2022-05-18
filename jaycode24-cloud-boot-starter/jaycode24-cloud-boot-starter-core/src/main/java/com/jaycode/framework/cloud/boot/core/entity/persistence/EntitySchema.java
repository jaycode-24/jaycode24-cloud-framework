package com.jaycode.framework.cloud.boot.core.entity.persistence;

import com.funi.framework.cloud.boot.core.entity.IdGenerateStrategy;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE})
@Retention(RUNTIME)
public @interface EntitySchema {
    IdGenerateStrategy idGenerateStrategy() default IdGenerateStrategy.SNOW_FLAKE;
}
