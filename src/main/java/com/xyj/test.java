package com.xyj;

import com.xyj.entity.User;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class test {
    public static void main(String[] args) {
//        AbstractApplicationContext ac=new ClassPathXmlApplicationContext("applicationContext.xml");
//        User bean = ac.getBean(User.class);
//        System.out.println(bean.getName());
//        System.out.println(bean.getE);
//        System.out.println(System.getProperty("sun.boot.class.path"));
//        System.out.println(System.getProperty("java.ext.dirs"));
//        System.out.println(System.getProperty("java.class.path"));
//        System.out.println(System.getProperty("java.security.manager"));

        String b=new String("11");
        String a="11";
        String c = b.intern();
        System.out.println(a==b);
        System.out.println(a==c);
        String s1=new String("ab")+new String("c");
        s1.intern();
        String s2="abc";
        System.out.println(s1==s2);
    }
}
