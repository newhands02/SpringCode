package com.xyj.classLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class testClassLoader {
    public static void main(String[] args) {
        UserClassLoader loader=new UserClassLoader("D:\\lib");
        try {
            Class<?> aClass = loader.loadClass("cm.xyj.test");
            if (aClass!=null){
                try {
                    Object o = aClass.newInstance();
                    Method say = aClass.getMethod("say", null);
                    say.invoke(o,null);
                }catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e){
                    e.printStackTrace();
                }
            }
        }catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
