package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.entity.Scmoannofiles;
import com.ruoyi.system.domain.entity.Scmoannotask;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface TaskMapper {
    @MapKey("task_id")
    Map<Object,Object> findTasksByUserId(@Param("id") Long id);
    void deleteTasksByTaskId(@Param("id") Long id);
    Scmoannofiles findTaskByTaskName(@Param("taskName") String taskName);
    void insertTask(Scmoannotask task);
    @MapKey("task_id")
    Map<Object,Object> findAllTasksWithUserInformation();
    void updateTaskStatus(@Param("id") Long id, @Param("status") Long status, @Param("details") String details);
    void updateTaskEndTime(@Param("id") Long id, @Param("time") Date time);

    void deleteTasksByTaskName(@Param("taskName") String taskName);
}
