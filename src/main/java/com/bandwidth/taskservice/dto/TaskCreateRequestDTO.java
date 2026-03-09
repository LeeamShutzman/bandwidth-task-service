package com.bandwidth.taskservice.dto;

import com.bandwidth.taskservice.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCreateRequestDTO implements Serializable {
    @NotBlank(message = "Task text cannot be empty.")
    @Size(max = 512, message = "Task text cannot exceed 512 characters.")
    private String text;

    @NotNull(message = "Priority must be specified.")
    private Priority priority; // Uses the Priority Enum for strong typing

    private Long userId;
}