package com.ai.sakha.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity
@Table(name = "tasks")

public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String taskname;
    private String description;
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Task{");
        sb.append("id=").append(id);
        sb.append(", taskname=").append(taskname);
        sb.append(", description=").append(description);
        sb.append('}');
        return sb.toString();
    }




    
}