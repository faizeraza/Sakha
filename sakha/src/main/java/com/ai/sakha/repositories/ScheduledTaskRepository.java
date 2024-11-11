package com.ai.sakha.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ai.sakha.entities.ScheduledTask;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
    
}
