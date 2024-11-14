package com.ai.sakha.services;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ai.sakha.entities.ScheduledTask;
import com.ai.sakha.entities.Task;

@Component
public class ServiceHandler {

    @Autowired
    TaskService taskService;

    public boolean addCronJob(ScheduledTask scheduledTask) throws IOException, InterruptedException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Long sId = scheduledTask.getId();
        Task task = scheduledTask.getTask();
        String taskname = task.getTaskname();
        String dateTime = dtf.format(scheduledTask.getScheduleDateTime());
        // String schedule = generateCronExpression(dateTime);
        String command = task.getCommand();
        String scriptName = (taskname.replaceAll("\\s", "")).toLowerCase() + sId + ".sh";
        String serviceName = (taskname.replaceAll("\\s", "")).toLowerCase() + sId + ".service";
        String timerName = (taskname.replaceAll("\\s", "")).toLowerCase() + sId + ".timer";
        System.out.println(dateTime);

        String scriptPath = "/home/admin/Desktop/Sakha/sakha/src/main/resources/scripts/";

        String scriptCommand = String.format("#!/bin/bash\n%s\n" + //
                "systemctl --user stop %s\n" + //
                "systemctl --user disable %s\n" + //
                "rm /home/admin/.config/systemd/user/%s", command, timerName, timerName, timerName);

        ProcessBuilder createScript = new ProcessBuilder("bash", "-c", "echo '" + scriptCommand + "' > " + scriptName + " && chmod +x " + scriptName);
        createScript.directory(new File(scriptPath));
        Process createScriptProcess = createScript.start();

        String service = String.format("[Unit]\nDescription=\"%s\"\n\n[Service]\nExecStart=\"%s\"", taskname, scriptPath + scriptName);

        String timer = String.format(
                "[Unit]\nDescription=\"%s\"\n\n[Timer]\nOnCalendar=%s\nUnit=%s\n\n[Install]\nWantedBy=multi-user.target",
                taskname, dateTime, serviceName);

        String servicePath = "/home/admin/.config/systemd/user/";

        ProcessBuilder setTimer = new ProcessBuilder("bash", "-c", "echo '" + timer + "' > " + timerName);
        setTimer.directory(new File(servicePath));
        Process setTimerProcess = setTimer.start();

        ProcessBuilder setService = new ProcessBuilder("bash", "-c", "echo '" + service + "' > " + serviceName);
        setService.directory(new File(servicePath));
        Process setServiceProcess = setService.start();

        ProcessBuilder executeService = new ProcessBuilder("bash", "-c", "systemctl --user daemon-reload");
        executeService.directory(new File(servicePath));
        Process executeServiceProcess = executeService.start();

        ProcessBuilder executeTimer = new ProcessBuilder("bash", "-c", "systemctl --user restart " + timerName);
        executeTimer.directory(new File(servicePath));
        Process executeTimerProcess = executeTimer.start();

        return createScriptProcess.waitFor() == 0 && setServiceProcess.waitFor() == 0 && setTimerProcess.waitFor() == 0 && executeTimerProcess.waitFor() == 0
                && executeServiceProcess.waitFor() == 0;

    }
}