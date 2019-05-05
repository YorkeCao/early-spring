package dev.yorke.early.spring.demo.service;

import dev.yorke.early.spring.ioc.annotation.EarlyComponent;

/**
 * @author Yorke
 */
@EarlyComponent
public class DemoServiceImpl implements DemoService {

    @Override
    public String greeting() {
        return "hello world";
    }
}
