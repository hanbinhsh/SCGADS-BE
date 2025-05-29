package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.domain.entity.Scmoannofiles;
import com.ruoyi.system.domain.entity.Scmoannotask;
import com.ruoyi.system.service.FilesServer;
import com.ruoyi.system.service.TaskServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.*;

import static com.ruoyi.common.utils.file.FileUtils.*;
import static com.ruoyi.system.controller.Utils.*;

@RestController
public class TaskController {
    @Autowired
    private TaskServer taskServer;

    @Autowired
    private FilesServer filesServer;

    @RequestMapping("/insertTask")
    @CrossOrigin(origins = "*")
    public Result insertTask(@RequestBody Scmoannotask task) {
        Timestamp timestamp = Timestamp.from(ZonedDateTime.now().toInstant());
        task.setStartTime(timestamp);
        task.setEndTime(timestamp);
        taskServer.insertTask(task);
        return Result.success();
    }

    @RequestMapping("/findTasksByUserID")
    @CrossOrigin(origins = "*")
    public Result<List<Object>> findTasksByUserID(@RequestParam long userID) {
    Map<Object, Object> taskMap = taskServer.findTasksByUserId(userID);
        List<Object> taskList = new ArrayList<>(taskMap.values()); // 提取值转换为列表
        return Result.success(taskList);
    }

    @RequestMapping("/checkExistsTaskByTaskName")
    @CrossOrigin(origins = "*")
    public Result checkExistsTaskByTaskName(@RequestParam String taskName) {
        if(taskServer.findTaskByTaskName(taskName) == null) {
            return Result.success();
        }
        else
            return Result.error("the taskName already exists");
    }

    @RequestMapping("/findTaskByTaskName")
    @CrossOrigin(origins = "*")
    public Result findTaskByTaskName(@RequestParam String taskName) {
         Scmoannotask task = taskServer.findTaskByTaskName(taskName);
         return Result.success(task);
    }

    @RequestMapping("/deleteTaskByTaskName")
    @CrossOrigin(origins = "*")
    public Result deleteTaskByTaskName(@RequestParam String userName ,@RequestParam String taskName){
        Scmoannofiles file  = filesServer.findFileByTaskName(taskName);
        if (file != null) {  // 先判断 file 是否为空
            if (file.getScRna_SeqFile() != null) {
                String fileName = file.getScRna_SeqFile();
                filesServer.updateFileHashNum(fileName, -1);
                if (filesServer.getFileHashNum(fileName) == 0)
                    deleteFile(getUploadLocation() + fileName);
            }
            if (file.getScAtac_SeqFile() != null) {
                String fileName = file.getScAtac_SeqFile();
                filesServer.updateFileHashNum(fileName, -1);
                if (filesServer.getFileHashNum(fileName) == 0)
                    deleteFile(getUploadLocation() + fileName);
            }
            if (file.getTagFile() != null) {
                String fileName = file.getTagFile();
                filesServer.updateFileHashNum(fileName, -1);
                if (filesServer.getFileHashNum(fileName) == 0)
                    deleteFile(getUploadLocation() + fileName);
            }
        }
        taskServer.deleteTasksByTaskName(taskName);

        try{
            String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
            deleteFolder(Path.of(baseDir + "/temp/Result/" + userName + "/" + taskName + "/"));
        }catch (Exception e){
            System.out.println("删除文件（夹）时发生错误：" + e.getMessage());
        }
        return Result.success();
    }


    @RequestMapping("/findAllTasksWithUserInformation")
    @CrossOrigin(origins = "*")
    public Result<Map<Object,Object>> findAllTasksWithUserInformation(){
        Map<Object, Object> tasksWithUserInfo = taskServer.findAllTasksWithUserInformation();

        // 遍历所有任务并转换用户头像
        for (Object task : tasksWithUserInfo.keySet()) {
            Object userInfo = tasksWithUserInfo.get(task);
            if (userInfo instanceof Map) {
                Map<String, Object> userMap = (Map<String, Object>) userInfo;
                if (userMap.get("avatar") instanceof byte[] avatarBytes) {
                    String base64Avatar = Base64.getEncoder().encodeToString(avatarBytes);
                    userMap.put("avatarBase64", base64Avatar); // 添加 Base64 编码字段
                }
            }
        }

        return Result.success(tasksWithUserInfo);
        //return Result.success(taskServer.findAllTasksWithUserInformation());
    }

    @RequestMapping("/updateTaskStatus")
    @CrossOrigin(origins = "*")
    public Result updateTaskStatus(@RequestParam("taskID") Long taskID, @RequestParam("status") Long status, @RequestParam("details") String details) {
        taskServer.updateTaskStatus(taskID, status, details);
        return Result.success();
    }

    @RequestMapping("/updateTaskStatusByTaskName")
    @CrossOrigin(origins = "*")
    public Result updateTaskStatusByTaskName(@RequestParam("taskName") String taskName, @RequestParam("status") Long status) {
        taskServer.updateTaskStatusByTaskName(taskName, status);
        return Result.success();
    }

    @RequestMapping("/findImgByTaskName")
    @CrossOrigin(origins = "*")
    @GetMapping("/findImgByTaskName")
    public ResponseEntity<?> findImageByTaskName(@RequestParam("taskName") String taskName,
                                                @RequestParam("userName") String userName) throws IOException {

        String imageDirectory = "./temp/Result/" + userName + "/" +taskName + "/";
        String fileName = taskName + ".png";
        Path imagePath = Paths.get(imageDirectory, fileName);

        // 读取图片文件
        byte[] imageBytes = Files.readAllBytes(imagePath);

        // 将图片转换为Base64编码
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        String imageDataUrl = "data:image/png;base64," + base64Image;

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "图片获取成功");
        response.put("taskName", taskName);
        response.put("imageBase64", base64Image);
        response.put("imageUrl", imageDataUrl);
        response.put("fileSize", imageBytes.length);

        return ResponseEntity.ok(response);
    }
}
