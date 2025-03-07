package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.domain.entity.Scmoannofiles;
import com.ruoyi.system.domain.entity.Scmoannoresult;
import com.ruoyi.system.domain.entity.Scmoannotask;
import com.ruoyi.system.service.FilesServer;
import com.ruoyi.system.service.TaskServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static com.ruoyi.system.controller.Utils.getUploadLocation;
import static com.ruoyi.common.utils.file.FileUtils.*;

@RestController
public class TaskController {
    @Autowired
    private TaskServer taskServer;

    @Autowired
    private FilesServer filesServer;

//    @RequestMapping("/insertTask")
//    @CrossOrigin(origins = "*")
//    public Result insertTask(@RequestBody Map<String, String> map) {
//        Timestamp timestamp = Timestamp.from(ZonedDateTime.now().toInstant());
//        Scmoannotask task = new Scmoannotask();
//        task.setTaskName(map.get("taskName"));
//        task.setStartTime(timestamp);
//        task.setEndTime(timestamp);
//        task.setUploaderId(Long.parseLong(map.get("userId")));
//        taskServer.insertTask(task);
//        return Result.success();
//    }

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
    public Result<List<Scmoannotask>> findTasksByUserID(@RequestParam long userID) {
        return Result.success(taskServer.findTasksByUserId(userID));
    }

    @RequestMapping("/findTaskByTaskName")
    @CrossOrigin(origins = "*")
    public Result findTasksByUserID(@RequestParam String taskName) {
        if(taskServer.findTaskByTaskName(taskName) == null) {
            return Result.success();
        }
        else
            return Result.error("the taskName already exists");
    }

    @RequestMapping("/deleteTaskByTaskName")
        @CrossOrigin(origins = "*")
        public Result deleteTaskByTaskName(@RequestParam String taskName) {
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

            Scmoannoresult result = filesServer.findResultByTaskName(taskName);
            if (result != null) {  // 先判断 result 是否为空
                if (result.getConfigFile() != null) {
                    deleteFile(getUploadLocation() + result.getConfigFile());
                }
                if (result.getDataFile() != null) {
                    deleteFile(getUploadLocation() + result.getDataFile());
                }
                if (result.getLableFile() != null) {
                    deleteFile(getUploadLocation() + result.getLableFile());
                }
            }

//            taskServer.deleteTasksByTaskId(taskID);
            taskServer.deleteTasksByTaskName(taskName);
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
}
