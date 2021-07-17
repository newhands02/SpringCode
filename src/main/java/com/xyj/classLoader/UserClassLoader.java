package com.xyj.classLoader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class UserClassLoader extends ClassLoader{
    private String path;

    public UserClassLoader(String path) {
        this.path = path;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String fileName=getFileName(name);
        File file=new File(path,fileName);
        try{
            FileInputStream fin=new FileInputStream(file);
            ByteArrayOutputStream bos=new ByteArrayOutputStream();
            int len=0;
            try{
                while ((len=fin.read())!=-1){
                    bos.write(len);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            byte[] data=bos.toByteArray();
            fin.close();
            bos.close();
            return defineClass(name,data,0, data.length);
        }catch (IOException e){
            e.printStackTrace();
        }
        return super.findClass(name);
    }
    private String getFileName(String name){
        int index=name.lastIndexOf(".");
        if (index==-1){
            return name+".class";
        }else {
            return name.substring(index+1)+".class";
        }
    }
}
