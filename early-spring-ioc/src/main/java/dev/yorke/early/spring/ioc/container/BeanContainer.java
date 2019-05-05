package dev.yorke.early.spring.ioc.container;

/**
 * @author Yorke
 */
public interface BeanContainer {

    /**
     * 获取 bean
     * @param beanName beanName
     * @return bean 实例
     */
    Object getBean(String beanName);

    /**
     * 是否包含指定 bean 实例
     * @param beanName beanName
     */
    boolean containsBean(String beanName);
}
