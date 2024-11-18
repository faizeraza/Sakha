package com.ai.sakha.services;

import java.io.BufferedReader;
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

    @Autowired
    private ServiceHandler serviceHandler;

    public Task createTask(Task task) throws Exception {
        ProcessBuilder authorization = new ProcessBuilder("bash", "-c",
                "unset GTK_PATH && zenity --password | sudo -S echo Authorized");
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
            if (serviceHandler.createService(task))
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

            if (serviceHandler.createService(task))
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

            if (serviceHandler.deleteService(task))
                taskRepository.delete(task);
            else
                throw new RuntimeException("An error occurred while creating a service for the task");
        }
        else
            throw new RuntimeException("Task not found");
    }

    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public void saveTask(Task task) {
        taskRepository.save(task); // Save the task to the database
    }

    public Process execute(String taskname) throws IOException, InterruptedException {
        return serviceHandler.executeService(taskname);
    }


}
