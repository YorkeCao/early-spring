package dev.yorke.early.spring.ioc.annotation;

import java.lang.annotation.*;

/**
 * @author Yorke
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EarlyAutowired {
    String value() default "";
}
