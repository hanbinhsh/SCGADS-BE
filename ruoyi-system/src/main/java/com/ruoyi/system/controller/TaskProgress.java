package com.ruoyi.system.controller;

import com.ruoyi.common.utils.spring.SpringUtils;
import com.ruoyi.system.domain.entity.Models;
import com.ruoyi.system.domain.entity.Result;
import com.ruoyi.system.domain.entity.Scmoannofiles;
import com.ruoyi.system.domain.entity.Scmoannotask;
import com.ruoyi.system.service.FilesServer;
import com.ruoyi.system.service.TaskServer;
import com.ruoyi.system.service.impl.ModelServer;
import net.sf.jsqlparser.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ruoyi.system.controller.FileController.decryptFile;
import static com.ruoyi.system.controller.Utils.*;

@RestController
public class TaskProgress {
    // 注释任务处理
    @RequestMapping("/predictProgress")
    @CrossOrigin(origins = "*")
    public Result predict(@RequestParam("taskName") String taskName,
                          @RequestParam("userName") String userName) throws IOException {
        // TODO 只做了注释处理，训练没做，且现在只有pbmc的sclth模型
        // 获取seq文件名
        FilesServer filesServer = SpringUtils.getBean(FilesServer.class);
        Scmoannofiles files = filesServer.findFilesByTaskName(taskName);
        // 获取任务
        TaskServer taskServer = SpringUtils.getBean(TaskServer.class);
        Scmoannotask task = taskServer.findTaskByTaskName(taskName);
        // 获取任务所选模型
        ModelServer ModelServer = SpringUtils.getBean(ModelServer.class);
        Models model = ModelServer.getModelById(task.getModelId());

        // 用户需要注释或训练的文件路径
        String atacPathBD   = getUploadLocation() + files.getScAtac_SeqFile();
        String rnaPathBD    = getUploadLocation() + files.getScRna_SeqFile();
        String labelPathBD  = getUploadLocation() + files.getTagFile();
        String atacPath     = getUploadLocation() + "temp/" + files.getScAtac_SeqFile();
        String rnaPath      = getUploadLocation() + "temp/" + files.getScRna_SeqFile();
        String labelPath    = getUploadLocation() + "temp/" + files.getTagFile();

        // 解密数据 TODO 如果开启了无需加密
        decryptFile(atacPathBD  , atacPath);
        decryptFile(rnaPathBD   , rnaPath);
        decryptFile(labelPathBD , labelPath);

        // Python 脚本路径
        String algorithmPath = getAlgorithmLocation();
        String pythonScriptPath = algorithmPath + ((model.getModelType().equals("single") || model.getModelType().equals("multi")) ? "annotation" : "denoising")
               + '/' + model.getModelName() + '/';
        // 脚本文件
        String predScriptPath  = pythonScriptPath + model.getPredictFilePath();
        String trainScriptPath = pythonScriptPath + model.getTrainFilePath();
        // 模型文件
        String checkpointPath = pythonScriptPath + "models/" + model.getModelPath();

        // 预测结果输出路径
        String outputNumPath = getResultLocation(userName, taskName) + "output_num.npy";
        String outputPath = getResultLocation(userName, taskName) + "output.npy";

        // 参数解析
        String parameters = task.getParameters();
        System.out.println(parameters);

        // 基本命令参数
        List<String> command = new ArrayList<>(Arrays.asList(
                "python", predScriptPath,
                "--atac_path", atacPath,
                "--rna_path", rnaPath,
                "--label_path", labelPath,
                "--checkpoint", checkpointPath,
                "--output_num_path", outputNumPath,
                "--output_path", outputPath,
                "--user_name", userName,
                "--task_name", taskName,
                "--task_type", task.getType()
        ));

        // 动态添加所有有值的参数
        if (parameters != null && !parameters.trim().isEmpty()) {
            String[] pairs = parameters.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    String value = kv[1].trim();
                    if (!key.isEmpty() && !value.isEmpty()) {
                        command.add("--" + key);
                        command.add(value);
                    }
                }
            }
        }

        // 启动进程
        System.out.println("预测任务 " + taskName + " 处理中"+task.getType());
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process = processBuilder.start();

        // (设置任务为处理中->模型预测->tsne->umap->设置任务为成功)
        //                        ^^^^^^^^^^^^^
        //                Python端调用tsneUmapChartProgress
        // 以上内容在python代码中完成处理

        // TODO 错误信息写入日志
//        // 输出错误信息（如果有）
//        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//        String line;
//        System.out.println("---- 注释任务 Python 错误输出 ----");
//        while ((line = errorReader.readLine()) != null) {
//            System.out.println(line);
//        }

        /// 生成图表和设置任务完成或者失败的代码在python中实现
        /// 由于预测和图表生成是顺序执行，避免重复设置任务状态

        return Result.success();
    }

    // 数据清洗任务处理

    // 模型训练任务处理

    /// 以下方法不在前端调用 ///

    @RequestMapping("/tsneUmapChartProgress")
    @CrossOrigin(origins = "*")
    public Result chartProgress(@RequestParam("type") String type,
                                 @RequestParam("taskName") String taskName,
                                 @RequestParam("userName") String userName,
                                 @RequestParam("seq_dir") String seq_dir,
                                 @RequestParam("label_dir") String label_dir,
                                 @RequestParam("outputnpyPath") String outputnpyPath) throws IOException {
        // 确定是否有真实标签
        String typePrefix = type != null && type.contains(":") ? type.split(":")[0] : "";
        String hasLabels = "training".equalsIgnoreCase(typePrefix) ? "true" : "false";
        // Python 脚本路径
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        String pythonScriptPath = baseDir + "/algorithm/visualization/dimension_reduction/dimension_reduction.py";
        String outputPath = baseDir + "/temp/Result/" + userName + '/' + taskName + '/';
        // 构造 ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python", pythonScriptPath,
                userName,
                taskName,
                hasLabels,
                outputnpyPath,
                outputPath,
                label_dir,
                seq_dir
        );
        // 启动进程
        Process process = processBuilder.start();
        System.out.println("生成umap降维图任务 " + taskName + " 处理中");
//        // 输出错误信息（如果有）
//        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//        String line;
//        System.out.println("---- 降维图生成任务 Python 错误输出 ----");
//        while ((line = errorReader.readLine()) != null) {
//            System.out.println(line);
//        }
        return Result.success();
    }

    // 任务处理完成
    @RequestMapping("/complete")
    @CrossOrigin(origins = "*")
    public Result<String> complete(@RequestParam String info) {
        System.out.println(info);
        return Result.success();
        // TODO 删除解密文件
    }
}
