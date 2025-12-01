package com.bandwidth.taskservice.dto;

import com.bandwidth.taskservice.model.Priority;
import lombok.Data;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

@Data
public class TaskUpdateRequestDTO implements Serializable {
    @Size(max = 512, message = "Task text cannot exceed 512 characters.")
    private String text;
    private Priority priority;
    private Boolean completed;
}