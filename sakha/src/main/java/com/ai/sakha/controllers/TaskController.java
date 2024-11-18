package com.ai.sakha.controllers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ai.sakha.entities.Task;
import com.ai.sakha.services.TaskService;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    // this is testing route
    @GetMapping("/basic")
    public String base(Model model) {
        model.addAttribute("message", "Hello, Thymeleaf!");
        return "base";  // This refetasksrs to home.html in the templates folder
    }

    @RequestMapping("/home")
    public String home(Model model) {
        List<Task> taskList = taskService.getAllTasks();  // Fetch tasks from service
        model.addAttribute("tasks", taskList);  // Add task list to the model
        model.addAttribute("task", new Task());  // Add empty tasks object for form binding
        return "index";  // This will render the home.html template
    }

    // 1. Create a new task
    @PostMapping("/create")
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        Task newTask;
        try {
            newTask = taskService.createTask(task);
            return ResponseEntity.status(201).body(newTask);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }  // 201 Created
    }

    // 2. Get all tasks
    @GetMapping("/all")  // Changed to GET
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    // Updates PutMaping Controller
    @PutMapping("/update")
    public ResponseEntity<String> updateTaskField(@RequestBody Task task) {
        try {
            // Call the service method to update the task
            String taskname = taskService.updateTask(task).getTaskname();

            return ResponseEntity.ok("Task " + taskname + " updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating task");
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) throws IOException, InterruptedException {

        taskService.deleteTask(id);
        return ResponseEntity.ok("Task Id :- " + id + " Deleted successfully!");

    }
    // TO BE DELETED
    // 5. Mark task as complete/incomplete
    // @PutMapping("/status/{id}")
    // public ResponseEntity<Task> changeTaskStatus(@PathVariable Long id, @RequestParam boolean completed) {
    //     Task task = taskService.updateTaskStatus(id, completed);
    //     return ResponseEntity.ok(task);
    // }
    // 6. Search for tasks
    // @GetMapping("/search")  // Changed to GET
    // public ResponseEntity<List<Task>> searchTasks(@RequestParam String query) {
    //     List<Task> tasks = taskService.searchTasks(query);
    //     return ResponseEntity.ok(tasks);
    // }

    @SuppressWarnings("unused")
    @GetMapping("/execute")
    public ResponseEntity<String> executeCommand(@RequestParam String taskname) {
        try (BufferedInputStream bis = new BufferedInputStream(taskService.execute(taskname).getInputStream())) {
            byte buffer[] = new byte[1024];
            int bytes;
            while ((bytes = bis.read(buffer)) != -1) {

            }
            String results = new String(buffer, StandardCharsets.UTF_8);
            return ResponseEntity.ok().body(results.trim());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(e.toString());
        }
        // return ResponseEntity.ok("Successfully executed command");
    }
}
