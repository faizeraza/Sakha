package com.ai.sakha.Controller;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ai.sakha.Entity.Task;
import com.ai.sakha.Sevices.TaskServices;


@Controller
@RequestMapping("/tasks")
public class sakhaController {

    @Autowired
    private TaskServices taskService;

    // this is testing routew
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
        return "home";  // This will render the home.html template
    }
    

    // 1. Create a new task
    @PostMapping("/create")
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        Task newTask = taskService.createTask(task);
        return ResponseEntity.status(201).body(newTask);  // 201 Created
    }

    // 2. Get all tasks
    @GetMapping("/all")  // Changed to GET
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    // Updates PutMaping Controller
    @PutMapping("/update/{id}")
public ResponseEntity<String> updateTaskField(@PathVariable Long id, 
                                              @RequestParam String fieldName, 
                                              @RequestParam String newValue) {
    try {
        // Prepare the updated task object
        Task updatedTask = new Task();
        updatedTask.setId(id);  // Set the ID of the task to update
        // Call the service method to update the task
        taskService.updaTask(id, newValue, fieldName.toLowerCase());

        return ResponseEntity.ok("Task updated successfully!");
    } catch (DateTimeParseException e) {
        return ResponseEntity.badRequest().body("Invalid date or time format");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating task");
    }
}

    // 5. Mark task as complete/incomplete
    @PutMapping("/status/{id}")
    public ResponseEntity<Task> changeTaskStatus(@PathVariable Long id, @RequestParam boolean completed) {
        Task task = taskService.updateTaskStatus(id, completed);
        return ResponseEntity.ok(task);
    }

    // 6. Search for tasks
    @GetMapping("/search")  // Changed to GET
    public ResponseEntity<List<Task>> searchTasks(@RequestParam String query) {
        List<Task> tasks = taskService.searchTasks(query);
        return ResponseEntity.ok(tasks);
    }
}

