package com.jaycode.framework.cloud.boot.core.entity;

import java.io.Serializable;
import java.util.function.Function;

public interface PropertyFunc<T, R> extends Function<T, R>, Serializable {

}
