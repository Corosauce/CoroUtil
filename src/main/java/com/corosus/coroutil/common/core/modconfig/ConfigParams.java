package com.corosus.coroutil.common.core.modconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify parameters about this config, comment, min, max
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigParams {
    String comment() default "";
    double min() default Double.MIN_VALUE;
    double max() default Double.MAX_VALUE;

}
