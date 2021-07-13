package com.xyj;

import com.xyj.entity.User;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class test {
    public static void main(String[] args) {
        AbstractApplicationContext ac=new ClassPathXmlApplicationContext("applicationContext.xml");
        User bean = ac.getBean(User.class);
        System.out.println(bean.getName());
//        System.out.println(bean.getE);
    }
}
