package com.bandwidth.taskservice.dto;

import com.bandwidth.taskservice.model.Priority;
import com.bandwidth.taskservice.model.Task;
import lombok.Data;
import java.time.LocalDateTime;
import java.io.Serializable;

@Data
public class TaskResponseDTO implements Serializable {
    private Long id;
    private Long userId;
    private String text;
    private Priority priority;
    private boolean completed;
    private LocalDateTime createdAt;

    public static TaskResponseDTO fromEntity(Task task) {
        TaskResponseDTO dto = new TaskResponseDTO();
        dto.setId(task.getId());
        dto.setUserId(task.getUserId());
        dto.setText(task.getText());
        dto.setPriority(task.getPriority());
        dto.setCompleted(task.isCompleted());
        dto.setCreatedAt(task.getCreatedAt());
        return dto;
    }
}