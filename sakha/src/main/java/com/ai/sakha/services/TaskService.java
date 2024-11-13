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

    public Task createTask(Task task) throws Exception {
        ProcessBuilder authCrontab = new ProcessBuilder("bash", "-c", "unset GTK_PATH && zenity --password | sudo -S echo Authorized ");
        // authCrontab.directory(new File("/home/admin/Desktop/Sakha/sakha/src/main/resources/"));
        Process process = authCrontab.start();
        String mesage = "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                mesage += line; // Print each line of output
            }
        }
        System.out.println(mesage);
        if (mesage.contains("Authorized")) {
            return taskRepository.save(task);
        } else {
            throw new Exception("Not Authorized");
        }
    }

    public Task getTask(String name) {
        return taskRepository.findByTaskname(name);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task updateTask(Task task) {
        Optional<Task> optionalTask = Optional.ofNullable(this.taskRepository.findByTaskname(task.getTaskname()));
        if (optionalTask.isPresent()) {
            Task taskToUpdate = optionalTask.get();
            task.setId(taskToUpdate.getId());
            return taskRepository.save(task);
        } else {
            throw new RuntimeException("Task not found");
        }
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    // TO BE DELETED
    // public Task updateTaskStatus(Long id, boolean completed) {
    //     Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
    //     return taskRepository.save(task);
    // }
    // @SuppressWarnings("unchecked")
    // public List<Task> searchTasks(String query) {
    //     return (List<Task>) taskRepository.findByTaskname(query);
    // }
    // Update task New Chnages 
    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public void saveTask(Task task) {
        taskRepository.save(task);  // Save the task to the database
    }

    public Process execute(String taskname) throws IOException {
        Task task = this.getTask(taskname);
        return new ProcessBuilder(
                task.getCommand().split(" ")).start();
    }
}
