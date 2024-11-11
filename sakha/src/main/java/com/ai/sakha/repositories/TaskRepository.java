package com.ai.sakha.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ai.sakha.entities.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Task findByTaskname(String taskname);

    Task deleteByTaskname(String taskname);
}
