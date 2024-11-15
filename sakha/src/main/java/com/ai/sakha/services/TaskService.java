package com.ai.sakha.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.sakha.entities.Task;
import com.ai.sakha.repositories.TaskRepository;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public Task createTask(Task task) throws Exception {
        ProcessBuilder authorization = new ProcessBuilder("bash", "-c",
                "unset GTK_PATH && zenity --password | sudo -S echo Authorized ");
        Process process = authorization.start();
        String message = "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                message += line; // Print each line of output
            }
        }
        System.out.println(message);
        if (message.contains("Authorized"))
            if (createService(task))
                return taskRepository.save(task);
            else
                throw new RuntimeException("An error occurred while creating a service for the task");
        else
            throw new RuntimeException("Not Authorized");

    }

    public Task getTask(String name) {
        return taskRepository.findByTaskname(name);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task updateTask(Task task) throws IOException, InterruptedException {
        Optional<Task> optionalTask = Optional.ofNullable(this.taskRepository.findByTaskname(task.getTaskname()));
        if (optionalTask.isPresent()) {
            Task taskToUpdate = optionalTask.get();
            task.setId(taskToUpdate.getId());

            if (createService(task))
                return taskRepository.save(task);
            else
                throw new RuntimeException("An error occurred while creating a service for the task");

        } else {
            throw new RuntimeException("Task not found");
        }
    }

    public void deleteTask(Long id) throws IOException, InterruptedException {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();

            if (deleteService(task))
                taskRepository.delete(task);
            else
                throw new RuntimeException("An error occurred while creating a service for the task");
        }
        else
            throw new RuntimeException("Task not found");
    }

    // TO BE DELETED
    // public Task updateTaskStatus(Long id, boolean completed) {
    // Task task = taskRepository.findById(id).orElseThrow(() -> new
    // RuntimeException("Task not found"));
    // return taskRepository.save(task);
    // }
    // @SuppressWarnings("unchecked")
    // public List<Task> searchTasks(String query) {
    // return (List<Task>) taskRepository.findByTaskname(query);
    // }
    // Update task New Chnages
    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public void saveTask(Task task) {
        taskRepository.save(task); // Save the task to the database
    }

    public Process execute(String taskname) throws IOException {
        Task task = this.getTask(taskname);
        return new ProcessBuilder(
                task.getCommand().split(" ")).start();
    }

    private boolean createService(Task task) throws IOException, InterruptedException {
        String command = task.getCommand();
        String taskname = task.getTaskname();
        String scriptName = (taskname.replaceAll("\\s", "")).toLowerCase() + ".sh";
        String serviceName = (taskname.replaceAll("\\s", "")).toLowerCase() + ".service";

        String scriptPath = "/home/admin/Desktop/Sakha/sakha/src/main/resources/scripts/";

        String scriptCommand = String.format("#!/bin/bash\n%s", command);

        ProcessBuilder createScript = new ProcessBuilder("bash", "-c",
                "echo '" + scriptCommand + "' > " + scriptName + " && chmod +x " + scriptName);
        createScript.directory(new File(scriptPath));
        Process createScriptProcess = createScript.start();

        String service = String.format(
                "[Unit]\nDescription=\"%s\"\n\n[Service]\nExecStart=\"%s\"",
                taskname, scriptPath + scriptName);

        String servicePath = "/home/admin/.config/systemd/user/";
        ProcessBuilder setService = new ProcessBuilder("bash", "-c", "echo '" + service + "' > " + serviceName);
        setService.directory(new File(servicePath));
        Process setServiceProcess = setService.start();

        return createScriptProcess.waitFor() == 0 && setServiceProcess.waitFor() == 0;
    }

    private boolean deleteService(Task task) throws IOException, InterruptedException {
        String taskname = task.getTaskname();
        String scriptName = (taskname.replaceAll("\\s", "")).toLowerCase() + ".sh";
        String serviceName = (taskname.replaceAll("\\s", "")).toLowerCase() + ".service";

        String scriptPath = "/home/admin/Desktop/Sakha/sakha/src/main/resources/scripts/";

        ProcessBuilder deleteScript = new ProcessBuilder("bash", "-c", "rm " + scriptName);
        deleteScript.directory(new File(scriptPath));
        Process deleteScriptProcess = deleteScript.start();

        String servicePath = "/home/admin/.config/systemd/user/";

        ProcessBuilder deleteService = new ProcessBuilder("bash", "-c", "rm " + serviceName);
        deleteService.directory(new File(servicePath));
        Process deleteServiceProcess = deleteService.start();

        return deleteScriptProcess.waitFor() == 0 && deleteServiceProcess.waitFor() == 0;
    }
}
