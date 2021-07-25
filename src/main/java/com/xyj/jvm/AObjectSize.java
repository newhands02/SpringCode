package com.xyj.jvm;

import org.openjdk.jol.info.ClassLayout;

public class AObjectSize {
    private static class T{
        int a;
        int b;
        boolean flag;
        String s="hello";
    }

    public static void main(String[] args) {
        T t=new T();
        t.hashCode();
        System.out.println(ClassLayout.parseInstance(t).toPrintable());
    }
}
