package dev.yorke.early.spring.ioc.annotation;

import java.lang.annotation.*;

/**
 * 标记由IOC容器注入的变量
 *
 * @author Yorke
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EarlyAutowired {
    String value() default "";
}
