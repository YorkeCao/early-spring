package dev.yorke.early.spring.ioc.annotation;

import java.lang.annotation.*;

/**
 * @author Yorke
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EarlyConfiguration {

    String value() default "";
}
