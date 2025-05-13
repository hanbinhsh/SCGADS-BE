package com.ruoyi.system.controller;

import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Utils {
    // 定位当前用户结果目录
    public static String getResultLocation(String userName, String taskName){
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        String tempDirPath = Paths.get(baseDir, "temp", "Result", userName, taskName).toString();
        File tempDir = new File(tempDirPath);
        if (!tempDir.exists()) {
            tempDir.mkdirs(); // 递归创建目录
        }
        return (tempDirPath + "/").replace("\\", "/");
    }

    // 定位当前用户上传文件的目录
    public static String getUploadLocation(){
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        String tempDirPath = baseDir + "/temp/Files";
        File tempDir = new File(tempDirPath);
        if (!tempDir.exists()) {
            tempDir.mkdirs(); // 递归创建目录
        }
        return (tempDirPath + "/").replace("\\", "/");
    }

    // 获取算法文件目录
    public static String getAlgorithmLocation() {
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        String tempDirPath = baseDir + "/algorithm";
        return (tempDirPath + "/").replace("\\", "/");
    }

    public static void deleteFolder(Path folderPath) throws IOException {
        Files.walkFileTree(folderPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file); // 先删除文件
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir); // 再删除文件夹
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
