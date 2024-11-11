package com.ai.sakha.services;

import java.time.LocalDateTime;
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

    @Autowired
    private TaskService taskService;

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
        scheduledTask.setTask(taskService.findTaskById(scheduledTask.getTask().getId()).orElseThrow());
        return scheduledTaskRepository.save(scheduledTask);
    }

    public ScheduledTask updateScheduledTask(ScheduledTask otherScheduledTask) {
        Optional<ScheduledTask> scheduleTaskOpt = scheduledTaskRepository.findById(otherScheduledTask.getId());
        if (scheduleTaskOpt.isPresent()) {
            ScheduledTask scheduledTask = scheduleTaskOpt.get();
            scheduledTask.setTask(taskService.findTaskById(otherScheduledTask.getTask().getId()).orElseThrow());
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

        throw new RuntimeException("Scheduled task with id " + id + " not found");
    }

    public String generateCronExpression(LocalDateTime dateTime) {
        if (dateTime.isBefore(LocalDateTime.now())) {
            return null; // If the date is in the past, skip scheduling
        }

        int second = dateTime.getSecond();
        int minute = dateTime.getMinute();
        int hour = dateTime.getHour();
        int dayOfMonth = dateTime.getDayOfMonth();
        int month = dateTime.getMonthValue();

        // Convert to cron format: "second minute hour day month ? year"
        return String.format("%d %d %d %d %d ? %d",
                second, minute, hour, dayOfMonth, month, dateTime.getYear());
    }
}
