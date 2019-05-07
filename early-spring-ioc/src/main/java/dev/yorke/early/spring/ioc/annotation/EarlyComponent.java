package dev.yorke.early.spring.ioc.annotation;

import java.lang.annotation.*;

/**
 * 标记委托IOC容器管理的类
 *
 * @author Yorke
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EarlyComponent {
    String value() default "";
}
