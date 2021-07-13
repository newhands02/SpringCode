package com.xyj.entity;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class User implements BeanNameAware, EnvironmentAware {
    private String name;
    private String beanName;
    private Environment environment;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName=name;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment=environment;
    }
}
