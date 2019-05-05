package dev.yorke.early.spring.demo.web;

import dev.yorke.early.spring.demo.service.DemoService;
import dev.yorke.early.spring.ioc.annotation.EarlyAutowired;
import dev.yorke.early.spring.ioc.annotation.EarlyComponent;

/**
 * @author Yorke
 */
@EarlyComponent
public class DemoController {

    @EarlyAutowired
    private DemoService demoService;

    public String greeting() {
        return demoService.greeting();
    }
}
