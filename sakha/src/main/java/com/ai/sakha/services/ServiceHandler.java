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
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.sakha.entities.ScheduledTaskDTO;

@Component
public class ServiceHandler {

    @Autowired
    TaskService taskService;

    public void addCronJob(ScheduledTaskDTO scheduledTaskDTO) throws IOException, InterruptedException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String taskname = scheduledTaskDTO.getTaskname();
        String dateTime = dtf.format(scheduledTaskDTO.getDateTime());
        // String schedule = generateCronExpression(dateTime);
        String command = taskService.getTask(taskname).getCommand();
        String serviceName = (taskname.replaceAll("\\s", "")).toLowerCase() + ".service";
        String timerName = (taskname.replaceAll("\\s", "")).toLowerCase() + ".timer";
        System.out.println(dateTime);
        String service = String.format("[Unit]\nDescription=\"%s\"\n\n[Service]\nExecStart=\"%s\"", taskname, command);

        String timer = String.format("[Unit]\nDescription=\"%s\"\n\n[Timer]\nOnCalendar=%s\nUnit=%s\n\n[Install]\nWantedBy=multi-user.target", taskname, dateTime, serviceName);

        String servicePath = "/home/admin/.config/systemd/user/";

        ProcessBuilder setService = new ProcessBuilder("bash", "-c", "echo '" + service + "' > " + serviceName);
        setService.directory(new File(servicePath));
        Process setServiceProcess = setService.start();

        ProcessBuilder setTimer = new ProcessBuilder("bash", "-c", "echo '" + timer + "' > " + timerName);
        setTimer.directory(new File(servicePath));
        Process setTimerProcess = setTimer.start();
        
        ProcessBuilder executeTimer = new ProcessBuilder("bash", "-c", "systemctl --user start " + timerName);
        executeTimer.directory(new File(servicePath));
        Process executeTimerProcess = executeTimer.start();
        
        ProcessBuilder executeService = new ProcessBuilder("bash", "-c", "systemctl --user daemon-reload");
        executeService.directory(new File(servicePath));
        Process executeServiceProcess = executeService.start();
        

        // Construct the cron job string
        // String cronJob = String.format("%s %s >> /home/admin/Desktop/Sakha/sakha/src/main/resources/logfile.log 2>&1", schedule, command);
        // String crontabDirPath = "/home/admin/Desktop/Sakha/sakha/src/main/resources/";
        // String crontabFilePath = crontabDirPath + "crontabList";


        // Retrieve current crontab and handle errors
        // if (!retrieveCurrentCrontab(crontabDirPath)) {
        //     System.err.println("Failed to retrieve the current crontab.");
        //     return;
        // }

        // Append new cron job
        // appendCronJob(crontabFilePath, cronJob);

        

        // Set the new crontab with sudo
        // if (setCrontabWithSudo(crontabDirPath)) {
        //     System.out.println("Crontab updated successfully.");
        // } else {
        //     System.err.println("Failed to update crontab.");
        // }
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

    private boolean retrieveCurrentCrontab(String crontabDirPath) throws IOException, InterruptedException {
        ProcessBuilder getCrontab = new ProcessBuilder("bash", "-c", "crontab -l > crontabList");
        getCrontab.directory(new File(crontabDirPath));
        Process getCrontabProcess = getCrontab.start();
        int exitCode = getCrontabProcess.waitFor();
        getCrontabProcess.destroy(); // Clean up the process
        return exitCode == 0;
    }

    private void appendCronJob(String crontabFilePath, String cronJob) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(crontabFilePath), StandardOpenOption.APPEND)) {
            writer.write(cronJob + "\n");
            System.out.println("Appended new cron job: " + cronJob);
        }
    }

    private boolean setCrontabWithSudo(String crontabDirPath) throws IOException, InterruptedException {

        ProcessBuilder setCrontab = new ProcessBuilder("bash", "-c", "crontab crontabList");
        setCrontab.directory(new File(crontabDirPath));
        Process setCrontabProcess = setCrontab.start();

        ProcessBuilder authCrontab = new ProcessBuilder("bash", "-c", "unset GTK_PATH && zenity --password | sudo -S crontab crontabList");
        authCrontab.directory(new File(crontabDirPath));
        Process authCrontabProcess = authCrontab.start();

        // Check for errors
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(authCrontabProcess.getErrorStream()))) {
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("Crontab Error: " + errorLine);
            }
        }

        return setCrontabProcess.waitFor() == 0 || authCrontabProcess.waitFor() == 0;
    }

    //deletion logic
    public void deleteCronJob(ScheduledTaskDTO scheduledTaskDTO) throws IOException, InterruptedException {
        String taskname = scheduledTaskDTO.getTaskname();
        String command = taskService.getTask(taskname).getCommand();
        LocalDateTime dateTime = scheduledTaskDTO.getDateTime();
        String schedule = generateCronExpression(dateTime);

        
        // Construct the cron job string to be deleted
        String cronJobToDelete = String.format("%s %s >> /home/admin/Desktop/Sakha/sakha/src/main/resources/logfile.log 2>&1", schedule, command);
        String crontabDirPath = "/home/admin/Desktop/Sakha/sakha/src/main/resources/";
        String crontabFilePath = crontabDirPath + "crontabList";
    
        // Retrieve current crontab
        if (!retrieveCurrentCrontab(crontabDirPath)) {
            System.err.println("Failed to retrieve the current crontab.");
            return;
        }
    
        // Read current crontab and remove the specified cron job
        removeCronJob(crontabFilePath, cronJobToDelete);
    
        // Set the new crontab
        if (setCrontabWithSudo(crontabDirPath)) {
            System.out.println("Crontab updated successfully.");
        } else {
            System.err.println("Failed to update crontab.");
        }
    
    }
    
    private void removeCronJob(String crontabFilePath, String cronJobToDelete) throws IOException {
        // Read all lines from the crontab file
        List<String> lines = Files.readAllLines(Paths.get(crontabFilePath));
        
        // Remove the specific cron job
        lines.removeIf(line -> line.contains(cronJobToDelete));
    
        // Write the updated lines back to the crontab file
        Files.write(Paths.get(crontabFilePath), lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("Removed cron job: " + cronJobToDelete);
    }
}
