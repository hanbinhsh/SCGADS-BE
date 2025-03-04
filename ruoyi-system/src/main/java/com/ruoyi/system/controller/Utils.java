package com.ruoyi.system.controller;

import java.io.File;
import java.nio.file.Paths;

public class Utils {
    public static String getResultLocation(String userName, String taskName){
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        String tempDirPath = Paths.get(baseDir, "temp", "Result", userName, taskName).toString();
        File tempDir = new File(tempDirPath);
        if (!tempDir.exists()) {
            tempDir.mkdirs(); // 递归创建目录
        }
        return tempDirPath + "/";
    }

    public static String getUploadLocation(){
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        String tempDirPath = baseDir + "/temp/Files";
        File tempDir = new File(tempDirPath);
        if (!tempDir.exists()) {
            tempDir.mkdirs(); // 递归创建目录
        }
        return tempDirPath + "/";
    }

    //    private String getResultLocation(){
    //        return "c:\\ScmoannoResult\\";
    //    }
    //
    //    private String getUploadLocation(){
    //        return "c:\\ScmoannoFiles\\";
    //    }
}
