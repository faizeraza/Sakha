package com.ai.sakha.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ai.sakha.Entity.Task;

public interface  TaskRepository extends JpaRepository<Task, Long> {
    Task findByTaskname(String taskname);
}
