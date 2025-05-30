package com.ruoyi.system.service;

import com.ruoyi.system.domain.entity.Scmoannofiles;
import com.ruoyi.system.domain.entity.Scmoannotask;

import java.util.List;
import java.util.Map;

public interface TaskServer {
    void insertTask(Scmoannotask task);
    Map<Object,Object> findTasksByUserId(Long id);
    void deleteTasksByTaskId(Long id);
    Map<Object,Object> findAllTasksWithUserInformation();
    void updateTaskStatus(Long id, Long status,String details);
    Scmoannotask findTaskByTaskName(String taskName);

    void deleteTasksByTaskName(String taskName);

    void updateTaskStatusByTaskName(String taskName, Long status);

    String findUserNameByTaskName(String taskName);

    Scmoannotask findTaskByShareId(Long shareId);
}
