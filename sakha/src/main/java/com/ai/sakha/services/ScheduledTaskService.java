package com.ai.sakha.services;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.sakha.entities.ScheduledTask;
import com.ai.sakha.repositories.ScheduledTaskRepository;

@Service
public class ScheduledTaskService {
    
    @Autowired
    private ScheduledTaskRepository scheduledTaskRepository;

    public Collection<ScheduledTask> getAll() {
        return scheduledTaskRepository.findAll();
    }

    public ScheduledTask getById(Long id) {
        Optional<ScheduledTask> scheduleTaskOpt = scheduledTaskRepository.findById(id);
        if (scheduleTaskOpt.isPresent()) 
            return scheduleTaskOpt.get();
        
        throw new RuntimeException("Scheduled task with id " + id + " not found");
    }

    public ScheduledTask createScheduledTask(ScheduledTask scheduledTask) {
        Optional<ScheduledTask> scheduleTaskOpt = scheduledTaskRepository.findById(scheduledTask.getId());
        if (scheduleTaskOpt.isPresent())
            throw new RuntimeException("Scheduled task with id " + scheduledTask.getId() + " already present");

        return scheduledTaskRepository.save(scheduledTask);
    }

    public ScheduledTask updateScheduledTask(ScheduledTask otherScheduledTask) {
        Optional<ScheduledTask> scheduleTaskOpt = scheduledTaskRepository.findById(otherScheduledTask.getId());
        if (scheduleTaskOpt.isPresent()) {
            ScheduledTask scheduledTask = scheduleTaskOpt.get();
            scheduledTask.setTask(otherScheduledTask.getTask());
            scheduledTask.setScheduleDateTime(otherScheduledTask.getScheduleDateTime());
            scheduledTask.setStatus(otherScheduledTask.getStatus());

            return scheduledTaskRepository.save(scheduledTask);
        }

        throw new RuntimeException("Scheduled task with id " + otherScheduledTask.getId() + " not found");
    }

    public ScheduledTask deleteScheduledTask(Long id) {
        Optional<ScheduledTask> scheduleTaskOpt = scheduledTaskRepository.findById(id);
        if (scheduleTaskOpt.isPresent()) {
            scheduledTaskRepository.deleteById(id);
            return scheduleTaskOpt.get();
        }

        throw new RuntimeException("Scheduled task with id " + id+ " not found");
    }

}
