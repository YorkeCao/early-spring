package dev.yorke.early.spring.demo;

import dev.yorke.early.spring.demo.web.DemoController;
import dev.yorke.early.spring.ioc.container.BeanContainer;
import dev.yorke.early.spring.ioc.container.EarlyBeanContainer;

/**
 * @author DELL
 */
public class DemoApplication {

    public static void main(String[] args) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        // 扫描并装载 bean
        BeanContainer beanContainer = new EarlyBeanContainer("dev.yorke.early.spring.demo");
        // 通过 bean 容器获取 bean
        DemoController demoController = (DemoController) beanContainer.getBean("demoController");
        // 执行实例方法
        System.out.println(demoController.greeting());
    }
}
