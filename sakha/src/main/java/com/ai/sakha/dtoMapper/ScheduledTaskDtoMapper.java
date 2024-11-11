package com.ai.sakha.dtoMapper;

import org.springframework.stereotype.Component;

import com.ai.sakha.entities.ScheduledTask;
import com.ai.sakha.entities.ScheduledTaskDTO;

@Component
public class ScheduledTaskDtoMapper {
    
    // Convert ScheduledTask entity to ScheduledTaskDTO
    public ScheduledTaskDTO toDTO(ScheduledTask scheduledTask) {
        if (scheduledTask == null) {
            return null;
        }
        String taskName = scheduledTask.getTask() != null ? scheduledTask.getTask().getTaskname() : null;
        String status = scheduledTask.getStatus() != null ? (scheduledTask.getStatus() ? "Active" : "Inactive") : null;

        return new ScheduledTaskDTO(taskName, scheduledTask.getScheduleDateTime(), status);
    }
}
