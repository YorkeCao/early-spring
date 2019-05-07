package dev.yorke.early.spring.demo.config;

import dev.yorke.early.spring.ioc.annotation.EarlyComponent;
import dev.yorke.early.spring.ioc.annotation.EarlyConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * @author Yorke
 */
@EarlyConfiguration
public class DemoConfiguration {

    @EarlyComponent
    public List<String> userList() {
        return Arrays.asList("Amy", "Bob");
    }
}
