package com.ai.sakha.services;

import java.io.IOException;
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

    public Task createTask(Task task) {
        return taskRepository.save(task);
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
        return new ProcessBuilder(task.getCommand()).start();
    }
}
