package com.ai.sakha.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.sakha.entities.ScheduledTask;
import com.ai.sakha.entities.ScheduledTaskDTO;
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
        if (scheduleTaskOpt.isPresent()) {
            return scheduleTaskOpt.get();
        }

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

    public static String generateCronExpression(LocalDateTime dateTime) {
        int minute = dateTime.getMinute();
        int hour = dateTime.getHour();
        int dayOfMonth = dateTime.getDayOfMonth();
        int month = dateTime.getMonthValue();
        int dayOfWeek = dateTime.getDayOfWeek().getValue();  // 1 (Monday) - 7 (Sunday)

        // Adjust dayOfWeek for cron (0 or 7 represents Sunday)
        dayOfWeek = (dayOfWeek == 7) ? 0 : dayOfWeek;

        // Construct the cron expression
        return String.format("%d %d %d %d %d", minute, hour, dayOfMonth, month, dayOfWeek);
    }

    public void addCronJob(ScheduledTaskDTO scheduledTaskDTO) throws IOException, InterruptedException {

        String taskname = scheduledTaskDTO.getTaskname();
        LocalDateTime dateTime = scheduledTaskDTO.getDateTime();
        String command = taskService.getTask(taskname).getCommand();
        String schedule = generateCronExpression(dateTime);
        String cronJob = schedule + " " + command;
        String crontabDirPath = "/home/admin/Desktop/Sakha/sakha/src/main/resources";
        String crontabFilePath = crontabDirPath + "/crontabList";

        ProcessBuilder getCrontab = new ProcessBuilder("bash", "-c", "crontab -l > crontabList");
        getCrontab.directory(new File(crontabDirPath));  // Set working directory
        Process getCrontabProcess = getCrontab.start();
        if (getCrontabProcess.waitFor() != 0) {
            System.err.println("Failed to retrieve the current crontab.");
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(crontabFilePath), StandardOpenOption.APPEND)) {
            writer.write(cronJob + "\n");
            System.out.println("Appended new cron job: " + cronJob);
        }

        ProcessBuilder setCrontab = new ProcessBuilder("bash", "-c", "crontab crontabList");
        setCrontab.directory(new File(crontabDirPath));
        Process setCrontabProcess = setCrontab.start();

        // Check for errors
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(setCrontabProcess.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("Crontab Error: " + errorLine);
            }
        }

        if (setCrontabProcess.waitFor() == 0) {
            System.out.println("Crontab updated successfully.");
        } else {
            System.err.println("Failed to update crontab.");
        }
    }
}
