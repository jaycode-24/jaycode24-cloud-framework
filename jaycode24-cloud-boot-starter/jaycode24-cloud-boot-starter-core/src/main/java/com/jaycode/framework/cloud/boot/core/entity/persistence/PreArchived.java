package com.jaycode.framework.cloud.boot.core.entity.persistence;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD})
@Retention(RUNTIME)
public @interface PreArchived {
}
