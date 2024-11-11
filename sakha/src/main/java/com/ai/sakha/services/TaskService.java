package com.ai.sakha.services;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ai.sakha.entities.Task;
import com.ai.sakha.repositories.TaskRepository;

@Service
public class TaskService{

    @Autowired
    private TaskRepository taskRepository;

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task getTask(String task) {
        return taskRepository.findByTaskname(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

        public Task updaTask(Long id, String newValue, String fieldName) {
            Optional<Task> optionalTask = this.taskRepository.findById(id);
            if (optionalTask.isPresent()) {
                Task taskToUpdate = optionalTask.get();
                switch (fieldName) {
                case "name" -> taskToUpdate.setTaskname(newValue);
                case "description" -> taskToUpdate.setDescription(newValue);
                
                default -> {
                    // return ResponseEntity.badRequest().body("Invalid field name");
                }
            }
                
                // Save the updated task
                return taskRepository.save(taskToUpdate);
            } else {
                throw new RuntimeException("Task not found");
            }
        }

  
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Task updateTaskStatus(Long id, boolean completed) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        return taskRepository.save(task);
    }

    @SuppressWarnings("unchecked")
    public List<Task> searchTasks(String query) {
        return (List<Task>) taskRepository.findByTaskname(query);
    }





    // Update task New Chnages 

    public Optional<Task> findTaskById(Long id) {
        return taskRepository.findById(id);
    }

    public void saveTask(Task task) {
        taskRepository.save(task);  // Save the task to the database
    }
}

