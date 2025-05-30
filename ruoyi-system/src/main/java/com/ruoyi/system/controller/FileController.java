package com.ruoyi.system.controller;

import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.entity.*;
import com.ruoyi.system.service.FilesServer;
import com.ruoyi.system.service.impl.TaskServer;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

import static com.ruoyi.common.utils.file.FileUtils.*;
import static com.ruoyi.system.controller.Utils.getResultLocation;
import static com.ruoyi.system.controller.Utils.getUploadLocation;

@RestController
public class FileController {

    @Autowired
    private FilesServer filesServer;
    @Autowired
    private TaskServer taskServer;

    @PostMapping("/uploadResult")
    @CrossOrigin(origins = "*")  // 跨域
    public Result uploadResult(@RequestParam("file") MultipartFile file,
                               @RequestParam("taskName") String taskName,
//                               @RequestParam("fileType") String fileType,
                               @RequestParam("userName") String userName) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());  // 文件名
        // 验证文件名有效性
        if (fileName.contains("..")) {
            return Result.error("文件名包含非法路径序列");
        }
//        String contentType = file.getContentType();  // 内容类型
        String name = file.getName();  // 表单域名
//        System.out.println(name+" "+fileName+" "+contentType);
        String realFilePath = getResultLocation(userName, taskName) + fileName;
        // 文件操作
        file.transferTo(new File(realFilePath));  // 移动到目标文件
        return Result.success();
    }


    @RequestMapping("/downloadResult")
    @CrossOrigin(origins = "*")  // 跨域
    public ResponseEntity<byte[]> downloadResult(@RequestParam("taskName") String taskName,
                                                 @RequestParam("type") String type,
                                                 @RequestParam("userName") String userName) throws IOException {
        userName = taskServer.findUserNameByTaskName(taskName);
        // 调用业务层接口的方法
        //Scmoannoresult result = filesServer.findResultByTaskName(taskName);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();  // 设置响应对象为二进制流
        builder.contentType(MediaType.APPLICATION_OCTET_STREAM);
        String fileName = URLEncoder.encode(taskName,"UTF-8");  // 设置下载的文件名
        builder.header("Access-Control-Expose-Headers", "Content-Disposition");
        builder.header("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
        builder.header("Accept-Ranges", "bytes");

        String filePaths = getResultLocation(userName, taskName)+ type + ".js";
        File dFile = new File(filePaths);
        return builder.body(FileUtils.readFileToByteArray(dFile));
    }

    @RequestMapping("/downloadTrainResult")
    @CrossOrigin(origins = "*")  // 跨域
    public ResponseEntity<byte[]> downloadTrainResult(@RequestParam("taskName") String taskName,
                                                      @RequestParam("type") String type,
                                                      @RequestParam("userName") String userName) throws IOException {
        userName = taskServer.findUserNameByTaskName(taskName);
        // 调用业务层接口的方法
        //Scmoannoresult result = filesServer.findResultByTaskName(taskName);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();  // 设置响应对象为二进制流
        builder.contentType(MediaType.APPLICATION_OCTET_STREAM);
        String fileName = URLEncoder.encode(taskName,"UTF-8");  // 设置下载的文件名
        builder.header("Access-Control-Expose-Headers", "Content-Disposition");
        builder.header("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
        builder.header("Accept-Ranges", "bytes");

        String filePaths = getResultLocation(userName, taskName) + "result/" + type + ".txt";
        File dFile = new File(filePaths);
        return builder.body(FileUtils.readFileToByteArray(dFile));
    }

    @PostMapping("/uploadOneFile")
    @CrossOrigin(origins = "*")  // 跨域
    public Result uploadOneFile(@RequestParam("file") MultipartFile file,
                                @RequestParam("taskName") String taskName,
                                @RequestParam("fileType") String fileType,
                                @RequestParam("hash") String hash) throws IOException {
        String fileName = file.getOriginalFilename();  // 文件名
        String contentType = file.getContentType();  // 内容类型
        String name = file.getName();  // 表单域名
        System.out.println(name + " " + fileName + " " + contentType);
        Timestamp timestamp = Timestamp.from(ZonedDateTime.now().toInstant());
        // 支持重复上传，uuid重新命名
        String randomFileName = UUID.randomUUID().toString();
        // 路径获取
        int suffixIndex = fileName.lastIndexOf(".");
        if (suffixIndex > 0) {  // 有后缀名
            randomFileName = randomFileName + fileName.substring(suffixIndex);
        }
        String realFilePath = getUploadLocation() + randomFileName;
        // 数据库包装
        filesServer.insertFileHash(hash, randomFileName);

        if (Objects.equals(fileType, "scRNASeqFile")) {
            filesServer.updateFiles1(randomFileName, taskName);
        } else if (Objects.equals(fileType, "scATACSeqFile")) {
            filesServer.updateFiles2(randomFileName, taskName);
        } else if (Objects.equals(fileType, "tagFile")) {
            filesServer.updateFiles3(randomFileName, taskName);
        }
        // 文件操作
        file.transferTo(new File(realFilePath));  // 移动到目标文件
        return Result.success();
    }

    @PostMapping("/getEncryptionKeys")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> getEncryptionKeys() {
        // 从数据库中获取密钥和IV
        EncryptionKeys encryptionKeys = filesServer.getEncryptionKeys(1);

        // 直接返回Hex字符串
        Map<String, String> keys = new HashMap<>();

        keys.put("aesKeyHex", encryptionKeys.getAesKey()); // 示例: "a3e4f5d6789c12b4567890abcdef1234"
        keys.put("ivHex", encryptionKeys.getIv());         // 示例: "112233445566778899aabbcc"

        return ResponseEntity.ok(keys);
    }

    @PostMapping("/fileHash")
    @CrossOrigin(origins = "*")
    public Result fileHash(@RequestBody Map<String, String> map) throws IOException {
        String fileName = filesServer.findFileByHash(map.get("hash"));
        if (fileName != null) {
            filesServer.updateFileHashNum(fileName, 1);
            if(Objects.equals(map.get("fileType"), "scRNASeqFile")){
                filesServer.updateFiles1(fileName, map.get("taskName"));
            }
            else if(Objects.equals(map.get("fileType"), "scATACSeqFile")){
                filesServer.updateFiles2(fileName, map.get("taskName"));
            }
            else if(Objects.equals(map.get("fileType"), "tagFile")){
                filesServer.updateFiles3(fileName, map.get("taskName"));
            }
            return Result.error("文件已存在");
        }
        return Result.success();
    }

    @RequestMapping("/insertFile")
    @CrossOrigin(origins = "*")
    public Result insertFile(@RequestBody Map<String,String> map) {
        Scmoannofiles files = new Scmoannofiles();
        files.setTaskName(map.get("taskName"));
        filesServer.insertFiles(files);
        return Result.success();
    }

    @GetMapping("/downloadTask")
    @CrossOrigin(origins = "*")
    public ResponseEntity<byte[]> downloadTask(@RequestParam("userName") String userName,
                                               @RequestParam("taskName") String taskName) throws Exception {
        // 1. 验证任务文件是否存在
        Scmoannofiles fileRecord = filesServer.findFileByTaskName(taskName);
        if (fileRecord == null) {
            return ResponseEntity.notFound().build();
        }
        // 2. 获取基础目录
        String baseDir = getResultLocation(userName, taskName);
        Path basePath = Paths.get(baseDir);
        // 4. 创建内存ZIP
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos)) {
            // 递归处理目录
            Files.walk(basePath)
                    .filter(path -> !path.equals(basePath)) // 排除根目录本身
                    .forEach(path -> {
                        try {
                            // 计算相对路径（用于ZIP中的条目名称）
                            String relativePath = basePath.relativize(path).toString();

                            if (Files.isDirectory(path)) {
                                // 如果是目录，在ZIP中创建目录条目
                                String dirEntryName = relativePath + "/"; // ZIP目录需要以/结尾
                                ZipArchiveEntry entry = new ZipArchiveEntry(dirEntryName);
                                zaos.putArchiveEntry(entry);
                                zaos.closeArchiveEntry();
                            } else {
                                // 如果是文件，读取、解密并添加到ZIP
                                byte[] encrypted = Files.readAllBytes(path);
//                                byte[] decrypted = decrypt(encrypted, aesKey, iv);
                                ZipArchiveEntry entry = new ZipArchiveEntry(relativePath);
                                zaos.putArchiveEntry(entry);
                                zaos.write(encrypted);
                                zaos.closeArchiveEntry();
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("处理文件失败: " + path, e);
                        }
                    });
            zaos.finish();
            // 5. 返回标准ZIP响应
            return ResponseEntity.ok()
                    .header("Content-Type", "application/zip")
                    .header("Content-Disposition", "attachment; filename=" + taskName + ".zip")
                    .body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(("下载失败: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/download")
    @CrossOrigin(origins = "*")
    public ResponseEntity<byte[]> download(@RequestParam String taskName) throws Exception {
        // 1. 验证任务文件是否存在
        Scmoannofiles fileRecord = filesServer.findFileByTaskName(taskName);
        if (fileRecord == null) {
            return ResponseEntity.notFound().build();
        }
        // 2. 构建安全文件路径（与上传逻辑一致）
        String baseDir = getUploadLocation(); // 使用相同的路径获取方法
        String[] storedFileNames = {
                fileRecord.getScRna_SeqFile(),
                fileRecord.getScAtac_SeqFile(),
                fileRecord.getTagFile()
        };

        // 3. 获取加密密钥（Hex格式直接转换）
        EncryptionKeys keys = filesServer.getEncryptionKeys(1);
        byte[] aesKey = DatatypeConverter.parseHexBinary(keys.getAesKey());  // Hex转字节
        byte[] iv = DatatypeConverter.parseHexBinary(keys.getIv());          // Hex转字节
        // 调试输出（可选）
//        System.out.println("解密密钥(Hex): " + keys.getAesKey());
//        System.out.println("解密IV(Hex): " + keys.getIv());

        // 4. 创建内存ZIP
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos)) {

            for (String storedName : storedFileNames) {
                Path filePath = Paths.get(baseDir, storedName);////
                // 读取加密文件（与上传时存储的格式完全一致）
                byte[] encrypted = Files.readAllBytes(filePath);
                // 解密（使用前端相同的算法参数）
                byte[] decrypted = decrypt(encrypted, aesKey, iv);
                // 恢复原始文件名逻辑（与上传时一致）
                String originalFileName = storedName.contains(".") ?
                        storedName.substring(0, storedName.lastIndexOf('.')) +
                                filePath.getFileName().toString().substring(filePath.getFileName().toString().lastIndexOf('.')) :
                        storedName;
                // 写入ZIP（保持原始文件名）
                ZipArchiveEntry entry = new ZipArchiveEntry(originalFileName);
                zaos.putArchiveEntry(entry);
                zaos.write(decrypted);
                zaos.closeArchiveEntry();
            }
            zaos.finish();
            // 5. 返回标准ZIP响应
            return ResponseEntity.ok()
                    .header("Content-Type", "application/zip")
                    .header("Content-Disposition", "attachment; filename=" + taskName + ".zip")
                    .body(baos.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(("下载失败: " + e.getMessage()).getBytes());
        }
    }

    // 解密方法
    private static byte[] decrypt(byte[] encryptedData, byte[] key, byte[] iv) throws Exception {
        // 密钥必须为16/24/32字节
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException(
                    "无效的AES密钥长度: " + key.length + "字节 (需16/24/32字节)\n" +
                            "实际密钥: " + bytesToHex(key)
            );
        }
        // IV必须为12字节（GCM标准）
        if (iv.length != 12) {
            throw new IllegalArgumentException(
                    "无效的IV长度: " + iv.length + "字节 (需12字节)\n" +
                            "实际IV: " + bytesToHex(iv)
            );
        }
        // 调试输出
//        System.out.println("输入数据长度: " + encryptedData.length + " bytes");
//        System.out.println("[解密输入] 密钥: " + bytesToHex(key));
//        System.out.println("[解密输入] IV: " + bytesToHex(iv));
//        System.out.println("[解密输入] 数据头: " + bytesToHex(Arrays.copyOfRange(encryptedData, 0, Math.min(16, encryptedData.length))));
        try {
            // ...原有解密逻辑...
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(key, "AES"),
                    new GCMParameterSpec(128, iv));

            byte[] decrypted = cipher.doFinal(encryptedData);

            // 4. 验证解密结果
            System.out.println("[解密成功] 输出长度: " + decrypted.length + " bytes");
            if (decrypted.length > 0) {
                System.out.println("[解密成功] 数据头: " + bytesToHex(Arrays.copyOfRange(decrypted, 0, Math.min(16, decrypted.length))));
            }



            return decrypted;
        } catch (AEADBadTagException e) {
            System.err.println("[解密失败] 认证标签不匹配：" + e.getMessage());
            throw new SecurityException("解密失败：密钥/IV不正确或数据被篡改", e);
        } catch (Exception e) {
            System.err.println("[解密失败] 其他错误：" + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw e;
        }
    }
    // 辅助方法：字节数组转Hex
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }



    /**
     * 解密加密文件并保存为新文件
     *
     * @param encryptedFilePath 加密文件路径（绝对路径）
     * @param decryptedFilePath 解密后文件保存路径（绝对路径）
     */

    public static void decryptFile(String encryptedFilePath, String decryptedFilePath) {
        try {
            FilesServer filesServer = SpringUtils.getBean(FilesServer.class);
            // 检查解密目录是否存在，不存在则创建
            File decryptedFile = new File(decryptedFilePath);
            File parentDir = decryptedFile.getParentFile();
            if (!parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created) {
                    System.err.println("无法创建目录: " + parentDir.getAbsolutePath());
                    return;
                }
            }


            // 读取加密文件内容
            byte[] encryptedData = Files.readAllBytes(Paths.get(encryptedFilePath));


            EncryptionKeys keys = filesServer.getEncryptionKeys(1);
            byte[] aesKey = DatatypeConverter.parseHexBinary(keys.getAesKey());  // Hex转字节
            byte[] iv = DatatypeConverter.parseHexBinary(keys.getIv());          // Hex转字节



            // 执行解密操作
            byte[] decryptedData = decrypt(encryptedData, aesKey, iv);


            // 写入解密后的数据到文件
            Files.write(Paths.get(decryptedFilePath), decryptedData);
        } catch (Exception e) {
            System.err.println("解密文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
