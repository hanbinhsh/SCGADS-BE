package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.*;
import com.ruoyi.system.service.FilesServer;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.ruoyi.common.utils.file.FileUtils.*;
import static com.ruoyi.system.controller.Utils.getResultLocation;
import static com.ruoyi.system.controller.Utils.getUploadLocation;

@RestController
public class FileController {

    @Autowired
    private FilesServer filesServer;

    @RequestMapping("/findResultByTaskName")
    @CrossOrigin(origins = "*")
    public Result findResultByTaskName(@RequestParam String taskName) {
        if(filesServer.findResultByTaskName(taskName)==null){
            return Result.success();
        }
        return Result.error("the taskName already exists");
    }

    @PostMapping("/uploadResult")
    @CrossOrigin(origins = "*")  // 跨域
    public Result uploadResult(@RequestParam("file") MultipartFile file,
                                @RequestParam("taskName") String taskName,
                                @RequestParam("fileType") String fileType,
                                @RequestParam("userName") String userName) throws IOException {
        String fileName = file.getOriginalFilename();  // 文件名
        String contentType = file.getContentType();  // 内容类型
        String name = file.getName();  // 表单域名
        System.out.println(name+" "+fileName+" "+contentType);
        Timestamp timestamp = Timestamp.from(ZonedDateTime.now().toInstant());
        // 支持重复上传，uuid重新命名
        String randomFileName = UUID.randomUUID().toString();
        // 路径获取
        int suffixIndex = fileName.lastIndexOf(".");
        if(suffixIndex > 0){  // 有后缀名
            randomFileName = randomFileName + fileName.substring(suffixIndex);
        }
        String realFilePath = getResultLocation(userName, taskName) + randomFileName;
        // 数据库包装
        Scmoannoresult result2=filesServer.findResultByTaskName(taskName);
        Scmoannoresult result = new Scmoannoresult();
        if(Objects.equals(fileType, "configjsFile")){
            if (result2.getConfigFile() != null) {
                deleteFile(getResultLocation(userName, taskName) + result2.getConfigFile());
//                result2.deleteFile(getResultLocation() + result2.getConfigFile());
            } else{
                System.out.println("configjsFile update failed");
            }
            result.setConfigFile(randomFileName);
            filesServer.updateResult1(result, taskName);
        }
        else if(Objects.equals(fileType, "datajsFile")){
            if (result2.getDataFile() != null) {
                deleteFile(getResultLocation(userName, taskName) + result2.getConfigFile());
//                result2.deleteFile(getResultLocation() + result2.getDataFile());
            } else{
                System.out.println("datajsFile update failed");
            }
            result.setDataFile(randomFileName);
            filesServer.updateResult2(result, taskName);
        }
        else if(Objects.equals(fileType, "lablejsFile")){
            if (result2.getLableFile() != null) {
                deleteFile(getResultLocation(userName, taskName) + result2.getConfigFile());
//                result2.deleteFile(getResultLocation() + result2.getLableFile());
            } else{
                System.out.println("lablejsFile update failed");
            }
            result.setLableFile(randomFileName);
            filesServer.updateResult3(result, taskName);
        }
        // 文件操作
        file.transferTo(new File(realFilePath));  // 移动到目标文件
        return Result.success();

    }


    @RequestMapping("/downloadResult")
    @CrossOrigin(origins = "*")  // 跨域
    public ResponseEntity<byte[]> downloadResult(@RequestParam("taskName") String taskName,
                                                 @RequestParam("type") String type,
                                                 @RequestParam("userName") String userName   ) throws IOException {
        // 调用业务层接口的方法
        Scmoannoresult result = filesServer.findResultByTaskName(taskName);
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();  // 设置响应对象为二进制流
        builder.contentType(MediaType.APPLICATION_OCTET_STREAM);
        String fileName = URLEncoder.encode(result.getTaskName(),"UTF-8");  // 设置下载的文件名
        builder.header("Access-Control-Expose-Headers", "Content-Disposition");
        builder.header("Content-Disposition", "attachment;filename*=UTF-8''" + fileName);
        builder.header("Accept-Ranges", "bytes");

        String filePaths = "paths";
        if(type.equals("config")) {
            filePaths = getResultLocation(userName, taskName)+result.getConfigFile();
        }
        else if(type.equals("label")) {
            filePaths = getResultLocation(userName, taskName)+result.getLableFile();
        }
        else if(type.equals("data")) {
            filePaths = getResultLocation(userName, taskName)+result.getDataFile();
        }
        File dFile = new File(filePaths);
        return builder.body(FileUtils.readFileToByteArray(dFile));
    }

    @PostMapping("/uploadOneFile")
    @CrossOrigin(origins = "*")  // 跨域
    public Result uploadOneFile(@RequestParam("file") MultipartFile file,
                                @RequestParam("taskName") String taskName,
                                @RequestParam("fileType") String fileType) throws IOException {
        String fileName = file.getOriginalFilename();  // 文件名
        String contentType = file.getContentType();  // 内容类型
        String name = file.getName();  // 表单域名
        System.out.println(name+" "+fileName+" "+contentType);
        Timestamp timestamp = Timestamp.from(ZonedDateTime.now().toInstant());
        // 支持重复上传，uuid重新命名
        String randomFileName = UUID.randomUUID().toString();
        // 路径获取
        int suffixIndex = fileName.lastIndexOf(".");
        if(suffixIndex > 0){  // 有后缀名
            randomFileName = randomFileName + fileName.substring(suffixIndex);
        }
        String realFilePath = getUploadLocation() + randomFileName;
        // 数据库包装

        Scmoannofiles files = new Scmoannofiles();

        if(Objects.equals(fileType, "scRNASeqFile")){
            files.setScRna_SeqFile(randomFileName);
            filesServer.updateFiles1(files, taskName);
        }
        else if(Objects.equals(fileType, "scATACSeqFile")){
            files.setScAtac_SeqFile(randomFileName);
            filesServer.updateFiles2(files, taskName);
        }
        else if(Objects.equals(fileType, "tagFile")){
            files.setTagFile(randomFileName);
            filesServer.updateFiles3(files, taskName);
        }
        // 文件操作
        file.transferTo(new File(realFilePath));  // 移动到目标文件
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

    @RequestMapping("/insertResult")
    @CrossOrigin(origins = "*")
    public Result insertResult(@RequestBody Map<String,String> map) {
        Scmoannoresult result = new Scmoannoresult();
        result.setTaskName(map.get("taskName"));
        filesServer.insertResult(result);
        return Result.success();
    }

    @GetMapping("/download")
    @CrossOrigin(origins = "*")
    public ResponseEntity<byte[]> download(@RequestParam String taskName) throws IOException {
        Scmoannofiles file = filesServer.findFileByTaskName(taskName);
        String[] filePaths = new String[3];
        filePaths[0] = getUploadLocation() + file.getScRna_SeqFile();
        filePaths[1] = getUploadLocation() + file.getScAtac_SeqFile();
        filePaths[2] = getUploadLocation() + file.getTagFile();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(baos);

            // 添加文件
            for (String filePath : filePaths) {
                try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
                    ZipArchiveEntry ze = new ZipArchiveEntry(filePath);
                    ze.setSize(Files.size(Paths.get(filePath)));
                    zaos.putArchiveEntry(ze);
                    IOUtils.copy(is, zaos);
                    zaos.closeArchiveEntry();
                }
            }

            zaos.close();

            // 创建一个文件资源对象，并设置相应的属性
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "multiple_files.zip");

            // 返回文件内容
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());
        } catch (IOException e) {
            // 处理异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
