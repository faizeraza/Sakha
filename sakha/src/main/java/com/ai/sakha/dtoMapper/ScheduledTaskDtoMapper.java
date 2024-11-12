package com.ai.sakha.dtoMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.sakha.entities.ScheduledTask;
import com.ai.sakha.entities.ScheduledTaskDTO;
import com.ai.sakha.entities.Task;
import com.ai.sakha.repositories.TaskRepository;

@Component
public class ScheduledTaskDtoMapper {

    @Autowired
    TaskRepository taskRepository;

    // Convert ScheduledTask entity to ScheduledTaskDTO
    public ScheduledTaskDTO toDTO(ScheduledTask scheduledTask) {
        if (scheduledTask == null) {
            return null;
        }
        String taskName = scheduledTask.getTask() != null ? scheduledTask.getTask().getTaskname() : null;
        String status = scheduledTask.getStatus() != null ? (scheduledTask.getStatus() ? "Active" : "Inactive") : null;

        return new ScheduledTaskDTO(taskName, scheduledTask.getScheduleDateTime(), status);
    }

    public ScheduledTask toEntity(ScheduledTaskDTO scheduledTaskDTO) {
        Task task = taskRepository.findByTaskname(scheduledTaskDTO.getTaskname());
        Boolean status = scheduledTaskDTO.getStatus() != null ? ("Active".equals(scheduledTaskDTO.getStatus())) : null;
        return new ScheduledTask(task, scheduledTaskDTO.getDateTime(), status);
    }
}
