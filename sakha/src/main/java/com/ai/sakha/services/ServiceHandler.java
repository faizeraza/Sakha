package com.ai.sakha.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.ai.sakha.entities.ScheduledTask;
import com.ai.sakha.entities.Task;

@Component
public class ServiceHandler {

        private final String scriptPath = System.getProperty("user.dir") + "/sakha/src/main/resources/scripts/";
        private final String servicePath = System.getProperty("user.home") + "/.config/systemd/user/";

        // -------------- script related ---------------------
        private boolean createScript(String scriptCommand, String scriptName) throws IOException, InterruptedException {
                String modifiedScriptName = scriptName + ".sh";
                ProcessBuilder createScript = new ProcessBuilder("bash", "-c",
                                "echo '" + scriptCommand + "' > " + modifiedScriptName + " && chmod +x "
                                                + modifiedScriptName);
                createScript.directory(new File(scriptPath));
                Process createScriptProcess = createScript.start();

                return createScriptProcess.waitFor() == 0;
        }

        // ---------- service related ----------
        public boolean createService(Task task) throws IOException, InterruptedException {
                String command = task.getCommand();
                String taskname = task.getTaskname();
                String modifiedTaskName = (taskname.replaceAll("\\s", "")).toLowerCase();
                String serviceName = modifiedTaskName + ".service";

                String scriptCommand = String.format("#!/bin/bash\n%s", command);

                boolean scriptCreated = createScript(scriptCommand, modifiedTaskName);

                String service = String.format(
                                "[Unit]\nDescription=\"%s\"\n\n[Service]\nExecStart=\"%s.sh\"",
                                taskname, scriptPath + modifiedTaskName);

                ProcessBuilder setService = new ProcessBuilder("bash", "-c", "echo '" + service + "' > " + serviceName);
                setService.directory(new File(servicePath));
                Process setServiceProcess = setService.start();

                return scriptCreated && setServiceProcess.waitFor() == 0;
        }

        public boolean deleteService(Task task) throws IOException, InterruptedException {
                String taskname = task.getTaskname();
                String scriptName = (taskname.replaceAll("\\s", "")).toLowerCase() + ".sh";
                String serviceName = (taskname.replaceAll("\\s", "")).toLowerCase() + ".service";

                ProcessBuilder deleteScript = new ProcessBuilder("bash", "-c", "rm " + scriptName);
                deleteScript.directory(new File(scriptPath));
                Process deleteScriptProcess = deleteScript.start();

                ProcessBuilder deleteService = new ProcessBuilder("bash", "-c", "rm " + serviceName);
                deleteService.directory(new File(servicePath));
                Process deleteServiceProcess = deleteService.start();

                return deleteScriptProcess.waitFor() == 0 && deleteServiceProcess.waitFor() == 0;
        }

        public Process executeService(String taskname) throws InterruptedException, IOException {
                String serviceName = (taskname.replaceAll("\\s", "")).toLowerCase() + ".service";

                ProcessBuilder executeTask = new ProcessBuilder("bash", "-c", "systemctl --user start " + serviceName);
                executeTask.directory(new File(servicePath));
                Process executeTaskProcess = executeTask.start();

                return executeTaskProcess;
        }

        // --------------- timer related -------------------
        public boolean addServiceTimer(ScheduledTask scheduledTask) throws IOException, InterruptedException {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                Long sId = scheduledTask.getId();
                Task task = scheduledTask.getTask();
                String taskname = task.getTaskname();
                String dateTime = dtf.format(scheduledTask.getScheduleDateTime());

                String modifiedTaskname = (taskname.replaceAll("\\s", "")).toLowerCase();
                String timerName = modifiedTaskname + sId + ".timer";

                String timer = String.format(
                                "[Unit]\nDescription=\"%s\"\n\n[Timer]\nOnCalendar=%s\nUnit=%s.service\n\n[Install]\nWantedBy=multi-user.target",
                                taskname, dateTime, modifiedTaskname);

                ProcessBuilder setTimer = new ProcessBuilder("bash", "-c", "echo '" + timer + "' > " + timerName);
                setTimer.directory(new File(servicePath));
                Process setTimerProcess = setTimer.start();

                ProcessBuilder reloadSystem = new ProcessBuilder("bash", "-c", "systemctl --user daemon-reload");
                reloadSystem.directory(new File(servicePath));
                Process reloadSystemProcess = reloadSystem.start();

                ProcessBuilder executeTimer = new ProcessBuilder("bash", "-c", "systemctl --user restart " + timerName);
                executeTimer.directory(new File(servicePath));
                Process executeTimerProcess = executeTimer.start();

                return setTimerProcess.waitFor() == 0 && executeTimerProcess.waitFor() == 0
                                && reloadSystemProcess.waitFor() == 0;

        }

        public List<Long> deleteElapsedTimers() throws InterruptedException, IOException {
                return deleteServiceTimer(getElapsedTimers());
        }

        public List<Long> deleteServiceTimer(String... timer) throws InterruptedException, IOException {

                String timers = String.join(" ", timer);
                System.out.println(timers);

                String cleaningScript = String.format("systemctl --user stop %s\n" + //
                                "systemctl --user disable %s\n" + //
                                "rm /home/admin/.config/systemd/user/%s", timers, timers, timers);

                boolean scriptCreated = createScript(cleaningScript, "cleaningscript");

                ProcessBuilder cleanTimers = new ProcessBuilder("bash", "-c", scriptPath + "cleaningscript.sh");
                cleanTimers.directory(new File(scriptPath));
                cleanTimers.start();
                if (scriptCreated)
                        return Arrays.stream(timer).map(s -> (s.replaceAll("[^0-9]", ""))).map(Long::valueOf)
                                        .collect(Collectors.toList());
                return List.of();
        }

        private static String[] getElapsedTimers() throws IOException {
                List<String> elapsedTimers = new ArrayList<>();

                // Run the `systemctl list-timers --all` command
                ProcessBuilder processBuilder = new ProcessBuilder("systemctl", "--user", "list-timers", "--all");
                Process process = processBuilder.start();

                // Read the command output
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        // Skip the header line
                        reader.readLine();
                        // Read the output line by line
                        while ((line = reader.readLine()) != null) {
                                // Ignore lines that are empty or not part of the timer list
                                if (line.trim().isEmpty() || line.startsWith("NEXT")) {
                                        continue;
                                }

                                // Split the line by whitespace
                                String[] parts = line.split("\\s+");

                                // If the "NEXT" column is "n/a" (typically the second column in the output)
                                if ("n/a".equals(parts[1])) {
                                        // The timer name is typically the second-to-last word (e.g., "example1.timer")
                                        String timerName = parts[parts.length - 2];
                                        elapsedTimers.add(timerName);
                                }
                        }
                }

                return elapsedTimers.toArray(new String[0]);
        }
}