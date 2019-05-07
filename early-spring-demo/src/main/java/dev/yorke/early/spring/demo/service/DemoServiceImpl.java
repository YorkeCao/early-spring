package dev.yorke.early.spring.demo.service;

import dev.yorke.early.spring.ioc.annotation.EarlyAutowired;
import dev.yorke.early.spring.ioc.annotation.EarlyComponent;

import java.util.List;

/**
 * @author Yorke
 */
@EarlyComponent
public class DemoServiceImpl implements DemoService {

    @EarlyAutowired
    private List<String> userList;

    @Override
    public String greeting() {
        return String.format("Hello, %s!", String.join(" & ", userList));
    }
}
