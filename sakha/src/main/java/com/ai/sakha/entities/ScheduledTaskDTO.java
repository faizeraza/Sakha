package com.ai.sakha.entities;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledTaskDTO {


    String taskname;
    LocalDateTime dateTime;
    String status;
}
