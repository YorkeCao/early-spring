package dev.yorke.early.spring.demo;

import dev.yorke.early.spring.demo.web.DemoController;
import dev.yorke.early.spring.ioc.container.BeanContainer;
import dev.yorke.early.spring.ioc.container.EarlyBeanContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Yorke
 */
public class DemoApplication {
    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException, InvocationTargetException {
        // 扫描并装载 bean
        BeanContainer beanContainer = new EarlyBeanContainer();
        // 通过 bean 容器获取 bean
        DemoController demoController = (DemoController) beanContainer.getBean("demoController");
        // 执行实例方法
        String message = demoController.greeting();
        log.info(message);
    }
}
