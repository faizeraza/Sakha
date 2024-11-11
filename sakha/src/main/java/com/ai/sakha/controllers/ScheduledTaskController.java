package com.ai.sakha.controllers;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ai.sakha.entities.ScheduledTask;
import com.ai.sakha.services.ScheduledTaskService;

@RestController
@RequestMapping("/scheduledTask")
public class ScheduledTaskController {

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @GetMapping("/get")
    public ResponseEntity<Collection<ScheduledTask>> getAllScheduledTasks() {
        return ResponseEntity.ok(scheduledTaskService.getAll());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ScheduledTask> getScheduledTask(@PathVariable Long id) {
        return ResponseEntity.ok(scheduledTaskService.getById(id));
    }

    @PostMapping("/create")
    public ResponseEntity<ScheduledTask> createScheduledTask(@RequestBody ScheduledTask scheduledTask) {
        return ResponseEntity.ok(scheduledTaskService.createScheduledTask(scheduledTask));
    }

    @PutMapping("/update")
    public ResponseEntity<ScheduledTask> updateScheduledTask(@RequestBody ScheduledTask scheduledTask) {
        return ResponseEntity.ok(scheduledTaskService.updateScheduledTask(scheduledTask));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ScheduledTask> deleteScheduledTask(@PathVariable Long id) {
        return ResponseEntity.ok(scheduledTaskService.deleteScheduledTask(id));
    }

}
