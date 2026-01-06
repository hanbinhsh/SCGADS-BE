package com.ruoyi.system.service.impl;

import com.ruoyi.system.domain.entity.Models;
import com.ruoyi.system.mapper.ModelMapper;
import com.ruoyi.system.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.nio.charset.Charset;

@Service
public class ModelServer implements ModelService {
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ModelCacheService modelCache;

    @Autowired
    private ModelsService modelsService;


    @Override
    public List<Models> getAllModels() {
        return modelMapper.getAllModels();
    }

    @Override
    public void deleteModel(Long modelId) {
        modelMapper.deleteModel(modelId);
        // 再删除缓存
        modelsService.refreshCache();
//        modelCache.evictModel(modelId);
    }

    @Override
    public void addModel(Models model) {
        System.out.println(model.getModelId());
        modelMapper.addModel(model);
        // 再写入缓存
//        System.out.println(model.getModelId());
        modelsService.refreshCache();
//        modelCache.cacheModelImage(model);
    }

    @Override
    public void updateModel(Models model) {
        modelMapper.updateModel(model);
        // 再更新缓存
        modelsService.refreshCache();
//        modelCache.updateCacheModel(model);
    }

    @Override
    public Models getModelById(Long modelId) {
        return modelMapper.getModelById(modelId);
    }

    @Override
    public void updateModelRemark(Long modelId, String remark) {
        modelMapper.updateModelRemark(modelId, remark);
        // 更新缓存
        modelsService.refreshCache();
//        modelCache.evictModel(modelId);
    }

    @Override
    public List<Models> findModelsByUserName(String userId) {
        return modelMapper.findModelsByUserName(userId);
    }

    @Override
    public void addChildModel(Models model) {
        modelMapper.addChildModel(model);
        modelsService.refreshCache();
    }

    @Override
    public String getBaseModelName(Long baseModel) {
        return modelMapper.getBaseModelName(baseModel);
    }

    @Override
    public void uploadAndUnzipPackage(MultipartFile file, String modelName, String modelType) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new Exception("上传文件不能为空");
        }

        // 1. 智能获取项目根目录
        String projectRoot = getScgadsBeRootPath();
        System.out.println("【后端日志】定位到项目根目录: " + projectRoot);

        // 2. 根据模型类型决定子目录
        String subPath;
        String typeLower = modelType.trim().toLowerCase();

        if (typeLower.contains("single") || typeLower.contains("multi")) {
            subPath = "algorithm" + File.separator + "annotation";
        } else if (typeLower.contains("deno")) {
            subPath = "algorithm" + File.separator + "denoising";
        } else {
            subPath = "algorithm" + File.separator + "others";
        }

        // 3. 拼接完整路径
        String cleanModelName = modelName.trim();
        String finalPath = Paths.get(projectRoot, subPath, cleanModelName).toString();
        File targetDir = new File(finalPath);

        // 4. 创建目录
        if (!targetDir.exists()) {
            boolean created = targetDir.mkdirs();
            if (!created) {
                throw new IOException("无法创建目标目录，请检查磁盘权限: " + finalPath);
            }
        }

        // 5. 执行解压 (这里会调用下面修改过的 unzipFile)
        try {
            unzipFile(file.getInputStream(), targetDir);
            System.out.println("【后端日志】解压成功! 存放路径: " + finalPath);
        } catch (IllegalArgumentException e) {
            // 如果 GBK 失败，尝试回退到 UTF-8 (为了兼容性)
            System.err.println("GBK解压失败，尝试使用 UTF-8 重试...");
            unzipFileWithUtf8(file.getInputStream(), targetDir);
        }
    }



    // -------------------------------------------------------------
    // 私有辅助方法
    // -------------------------------------------------------------

    private String getScgadsBeRootPath() {
        String userDir = System.getProperty("user.dir");
        if (userDir.contains("SCGADS-BE")) {
            int index = userDir.indexOf("SCGADS-BE");
            return userDir.substring(0, index + "SCGADS-BE".length());
        }
        File current = new File(userDir);
        File temp = current;
        while (temp != null) {
            if (temp.getName().equalsIgnoreCase("SCGADS-BE")) {
                return temp.getAbsolutePath();
            }
            temp = temp.getParentFile();
        }
        return userDir;
    }

    /**
     * 【关键修改】默认使用 GBK 解压 (兼容 Windows 中文环境)
     */
    private void unzipFile(InputStream inputStream, File targetDir) throws IOException {
        // 使用 GBK 编码，解决 malformed input 错误
        Charset gbkCharset = Charset.forName("GBK");

        try (ZipInputStream zis = new ZipInputStream(inputStream, gbkCharset)) {
            processZipStream(zis, targetDir);
        }
    }

    /**
     * 备用方案：使用 UTF-8 解压 (兼容 Linux/Mac 生成的压缩包)
     */
    private void unzipFileWithUtf8(InputStream inputStream, File targetDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {
            processZipStream(zis, targetDir);
        }
    }

    /**
     * 提取公共的解压逻辑
     */
    private void processZipStream(ZipInputStream zis, File targetDir) throws IOException {
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            // 忽略 Mac 系统生成的隐藏文件
            if (!zipEntry.getName().contains("__MACOSX")) {
                File destFile = new File(targetDir, zipEntry.getName());

                // 防止 Zip Slip 漏洞
                String destDirPath = targetDir.getCanonicalPath();
                String destFilePath = destFile.getCanonicalPath();
                if (!destFilePath.startsWith(destDirPath + File.separator)) {
                    throw new IOException("非法路径检测: " + zipEntry.getName());
                }

                if (zipEntry.isDirectory()) {
                    destFile.mkdirs();
                } else {
                    File parent = destFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
            zipEntry = zis.getNextEntry();
        }
    }
}
