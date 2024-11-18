package com.ai.sakha.services;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.sakha.dtoMapper.ScheduledTaskDtoMapper;
import com.ai.sakha.entities.ScheduledTask;
import com.ai.sakha.entities.ScheduledTaskDTO;
import com.ai.sakha.entities.Task;
import com.ai.sakha.repositories.ScheduledTaskRepository;

import jakarta.annotation.PostConstruct;

@Service
public class ScheduledTaskService {

    @Autowired
    private ScheduledTaskRepository scheduledTaskRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    ScheduledTaskDtoMapper scheduledTaskDtoMapper;

    @Autowired
    ServiceHandler serviceHandler;

    @PostConstruct
    private void sync() throws IOException, InterruptedException {
        for (ScheduledTask scheduledTask : scheduledTaskRepository.findAll())
            serviceHandler.addServiceTimer(scheduledTask);
    }

    public Collection<ScheduledTask> getAll() {
        return scheduledTaskRepository.findAll();
    }

    public ScheduledTask getById(Long id) {
        Optional<ScheduledTask> scheduleTaskOpt = scheduledTaskRepository.findById(id);
        if (scheduleTaskOpt.isPresent()) {
            return scheduleTaskOpt.get();
        }

        throw new RuntimeException("Scheduled task with id " + id + " not found");
    }

    public ScheduledTaskDTO createScheduledTask(ScheduledTaskDTO scheduledTaskDTO)
            throws IOException, InterruptedException {
        ScheduledTask result = scheduledTaskRepository.save(scheduledTaskDtoMapper.toEntity(scheduledTaskDTO));
        List<Long> elapsedTimersIds = serviceHandler.deleteElapsedTimers();
        System.out.println(elapsedTimersIds);
        scheduledTaskRepository.deleteAllById(elapsedTimersIds);
        System.out.println(serviceHandler.addServiceTimer(result));
        ScheduledTaskDTO response = scheduledTaskDtoMapper.toDTO(result);
        return response;
    }

    public ScheduledTask updateScheduledTask(ScheduledTask otherScheduledTask)
            throws IOException, InterruptedException {
        Optional<ScheduledTask> scheduleTaskOpt = scheduledTaskRepository.findById(otherScheduledTask.getId());
        if (scheduleTaskOpt.isPresent()) {
            ScheduledTask scheduledTask = scheduleTaskOpt.get();
            scheduledTask.setTask(taskService.findTaskById(otherScheduledTask.getTask().getId()).orElseThrow());
            scheduledTask.setScheduleDateTime(otherScheduledTask.getScheduleDateTime());
            scheduledTask.setStatus(otherScheduledTask.getStatus());
            System.out.println(serviceHandler.addServiceTimer(scheduledTask));
            return scheduledTaskRepository.save(scheduledTask);
        }

        throw new RuntimeException("Scheduled task with id " + otherScheduledTask.getId() + " not found");
    }

    public ScheduledTaskDTO deleteScheduledTask(Long id) throws IOException, InterruptedException {
        Optional<ScheduledTask> scheduleTaskOpt = scheduledTaskRepository.findById(id);
        if (scheduleTaskOpt.isPresent()) {
            scheduledTaskRepository.deleteById(id);
            ScheduledTask scheduledTask = scheduleTaskOpt.get();
            ScheduledTaskDTO scheduledTaskDto = scheduledTaskDtoMapper.toDTO(scheduledTask);
            String timer = (scheduledTaskDto.getTaskname().replaceAll("\\s", "")).toLowerCase() + +id + ".timer";
            System.out.println(timer);
            serviceHandler.deleteServiceTimer(timer);
            return scheduledTaskDto;
        }

        throw new RuntimeException("Scheduled task with id " + id + " not found");
    }
}
