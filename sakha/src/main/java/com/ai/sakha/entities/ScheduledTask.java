package com.ai.sakha.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
@Data
@Entity
@Table(name = "scheduledtask")
public class ScheduledTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="taskid")
    private Task task;
    
    private String scheduleTime;
    private String scheduleDate; 
    private boolean status;
}
