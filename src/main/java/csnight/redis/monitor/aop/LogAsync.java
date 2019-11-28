package csnight.redis.monitor.aop;

import java.lang.annotation.*;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAsync {
    String module() default "";

    String op() default "";
}
