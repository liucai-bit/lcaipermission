package com.liucai.core.util.lcaidb;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TableId {
    String tableId() default "";
}
