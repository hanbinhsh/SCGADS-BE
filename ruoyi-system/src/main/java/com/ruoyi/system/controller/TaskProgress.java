package com.ruoyi.system.controller;

import com.ruoyi.system.domain.entity.Result;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class TaskProgress {
    // 注释任务处理
    @RequestMapping("/predictProgress")
    @CrossOrigin(origins = "*")
    public Result predict(@RequestParam("taskName") String taskName,
                          @RequestParam("userName") String userName) throws IOException {
        // Python 脚本路径
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        String pythonScriptPath = baseDir + "/algorithm/prediction/predict.py"; // TODO: 预测脚本路径

        // 输入数据路径
        String atacPath = "'G:/Projects/seqData/mouse_skin_shareseq_rna_10k/atac.h5ad'"; // TODO
        String rnaPath = "'G:/Projects/seqData/mouse_skin_shareseq_rna_10k/rna.h5ad'"; // TODO
        String labelPath = "'G:/Projects/seqData/mouse_skin_shareseq_rna_10k/Label.csv'"; // TODO

        // 模型文件路径
        String checkpointPath = "'./result/mouse_skin_shareseq_rna_10k/train_best_1919810.ckpt'"; // TODO

        // 预测结果输出路径
        String outputNumPath = baseDir + "/temp/Result/" + userName + '/' + taskName + "/output_num.npy"; // TODO
        String outputPath = baseDir + "/temp/Result/" + userName + '/' + taskName + "/output.npy"; // TODO

        // 构造 ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(
                "python", pythonScriptPath,
                "--atac_path", atacPath,
                "--rna_path", rnaPath,
                "--label_path", labelPath,
                "--checkpoint", checkpointPath,
                "--output_num_path", outputNumPath,
                "--output_path", outputPath
        );

        // 启动进程
        Process process = processBuilder.start();
        System.out.println("预测任务 " + taskName + " 处理中");

        return Result.success();
    }

    // 降维图生成
    @RequestMapping("/tsneProgress")
    @CrossOrigin(origins = "*")
    public Result tsneProgress(@RequestParam("type") String type,
                                 @RequestParam("taskName") String taskName,
                                 @RequestParam("userName") String userName) throws IOException {
        // 确定是否有真实标签
        String hasLabels = "training".equals(type) ? "true" : "false";
        // Python 脚本路径
        String baseDir = System.getProperty("user.dir"); // 获取当前项目的根目录
        String pythonScriptPath = baseDir + "/algorithm/visualization/tsne_chart/tsne.py";
        String outputPath = baseDir + "/temp/Result/" + userName + '/' + taskName + '/';
        // 降维图文件路径
        String seq_dir = "'G:/Projects/seqData/mouse_skin_shareseq_rna_10k/rna.h5ad'"; // TODO
        String label_dir = "'G:/Projects/seqData/mouse_skin_shareseq_rna_10k/Label.csv'"; // TODO
        String outputnpyPath = "G:/JAVA/RuoYi-Vue-master/algorithm/visualization/tsne_chart/output.npy"; // TODO
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
        System.out.println("生成降维图任务 " + taskName + " 处理中");
        return Result.success();
    }

    // 数据清洗任务处理

    // 模型训练任务处理

    // 任务处理完成
    @RequestMapping("/complete")
    @CrossOrigin(origins = "*")
    public Result<String> complete(@RequestParam String info) {
        System.out.println(info);
        return Result.success();
    }
}
