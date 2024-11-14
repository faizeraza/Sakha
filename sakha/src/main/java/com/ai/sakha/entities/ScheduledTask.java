package com.ai.sakha.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Entity
@Table(name = "scheduledtask")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "taskid")
    private Task task;

    private LocalDateTime scheduleDateTime;
    private Boolean status;

    public ScheduledTask(Task task, LocalDateTime scheduleDateTime, Boolean status) {
        this.task = task;
        this.scheduleDateTime = scheduleDateTime;
        this.status = status;
    }

}
