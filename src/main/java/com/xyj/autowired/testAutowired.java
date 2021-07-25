package com.xyj.autowired;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

public class testAutowired {
    public static void main(String[] args) {
        UserController userController=new UserController();
        Class<? extends UserController> clazz = userController.getClass();
        UserService userService=new UserService();
        Stream.of(clazz.getDeclaredFields()).forEach(field -> {
            String name=field.getName();
            MyAutowired annotation = field.getAnnotation(MyAutowired.class);
            if (annotation!=null){
                field.setAccessible(true);
                Class<?> type = field.getType();
                try {
                    Object o = type.getConstructor(null).newInstance();
                    field.set(userController,o);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println(userController.getUserService());
        System.out.println(userService);
    }
}
