package com.ai.sakha.services;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.sakha.dtoMapper.ScheduledTaskDtoMapper;
import com.ai.sakha.entities.ScheduledTask;
import com.ai.sakha.entities.ScheduledTaskDTO;
import com.ai.sakha.repositories.ScheduledTaskRepository;

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

    public ScheduledTaskDTO createScheduledTask(ScheduledTaskDTO scheduledTaskDTO) throws IOException, InterruptedException {
        ScheduledTask result = scheduledTaskRepository.save(scheduledTaskDtoMapper.toEntity(scheduledTaskDTO));
        System.out.println(serviceHandler.addCronJob(result));
        ScheduledTaskDTO response = scheduledTaskDtoMapper.toDTO(result);
        return response;
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

    public ScheduledTask deleteScheduledTask(Long id) throws IOException, InterruptedException {
        Optional<ScheduledTask> scheduleTaskOpt = scheduledTaskRepository.findById(id);
        if (scheduleTaskOpt.isPresent()) {
            scheduledTaskRepository.deleteById(id);
            ScheduledTask scheduledTask = scheduleTaskOpt.get();
            ScheduledTaskDTO scheduledTaskDto = scheduledTaskDtoMapper.toDTO(scheduledTask);
            return scheduleTaskOpt.get();
        }

        throw new RuntimeException("Scheduled task with id " + id + " not found");
    }

    // TO BE DELETED JUST FOR REF. KEPT IT
    // public static String generateCronExpression(LocalDateTime dateTime) {
    //     int minute = dateTime.getMinute();
    //     int hour = dateTime.getHour();
    //     int dayOfMonth = dateTime.getDayOfMonth();
    //     int month = dateTime.getMonthValue();
    //     int dayOfWeek = dateTime.getDayOfWeek().getValue();  // 1 (Monday) - 7 (Sunday)
    //     // Adjust dayOfWeek for cron (0 or 7 represents Sunday)
    //     dayOfWeek = (dayOfWeek == 7) ? 0 : dayOfWeek;
    //     // Construct the cron expression
    //     return String.format("%d %d %d %d %d", minute, hour, dayOfMonth, month, dayOfWeek);
    // }
    // private void addCronJob(ScheduledTaskDTO scheduledTaskDTO, String sudoPassword) throws IOException, InterruptedException {
    //     String taskname = scheduledTaskDTO.getTaskname();
    //     LocalDateTime dateTime = scheduledTaskDTO.getDateTime();
    //     String command = taskService.getTask(taskname).getCommand();
    //     // Prepend 'sudo' to the command
    //     String sudoCommand = command;
    //     String schedule = generateCronExpression(dateTime);
    //     String cronJob = "* * * * *" + " " + sudoCommand + " >> /home/admin/Desktop/Sakha/sakha/src/main/resources/logfile.log 2>&1";
    //     String crontabDirPath = "/home/admin/Desktop/Sakha/sakha/src/main/resources/";
    //     String crontabFilePath = crontabDirPath + "crontabList";
    //     // Retrieve current crontab
    //     ProcessBuilder getCrontab = new ProcessBuilder("bash", "-c", "crontab -l > crontabList");
    //     getCrontab.directory(new File(crontabDirPath));
    //     Process getCrontabProcess = getCrontab.start();
    //     if (getCrontabProcess.waitFor() != 0) {
    //         System.err.println("Failed to retrieve the current crontab.");
    //         return;
    //     }
    //     // Append new cron job
    //     try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(crontabFilePath), StandardOpenOption.APPEND)) {
    //         writer.write(cronJob + "\n");
    //         System.out.println("Appended new cron job: " + cronJob);
    //     }
    //     getCrontabProcess.destroy();
    //     // Set the new crontab with sudo
    //     ProcessBuilder setCrontab = new ProcessBuilder("bash", "-c", "crontab crontabList");
    //     setCrontab.directory(new File(crontabDirPath));
    //     Process setCrontabProcess = setCrontab.start();
    //     ProcessBuilder authCrontab = new ProcessBuilder("bash", "-c", "unset GTK_PATH && zenity --password |sudo -S crontab crontabList");
    //     authCrontab.directory(new File(crontabDirPath));
    //     Process authCrontabProcess = authCrontab.start();
    //     // Check for errors
    //     try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(setCrontabProcess.getErrorStream()))) {
    //         String errorLine;
    //         while ((errorLine = errorReader.readLine()) != null) {
    //             System.err.println("Crontab Error: " + errorLine);
    //         }
    //     }
    //     if (setCrontabProcess.waitFor() == 0 || authCrontabProcess.waitFor() == 0) {
    //         System.out.println("Crontab updated successfully.");
    //     } else {
    //         System.err.println("Failed to update crontab.");
    //     }
    // }
}
