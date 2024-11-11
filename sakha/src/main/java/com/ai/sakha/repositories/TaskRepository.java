package com.ai.sakha.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ai.sakha.entities.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Task findByTaskname(String taskname);
}
