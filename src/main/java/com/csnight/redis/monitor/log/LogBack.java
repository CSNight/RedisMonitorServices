package com.csnight.redis.monitor.log;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogBack {
    String value() default "";
}
