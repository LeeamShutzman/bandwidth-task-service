package com.bandwidth.taskservice.controller;

import com.bandwidth.taskservice.dto.TaskCreateRequestDTO;
import com.bandwidth.taskservice.dto.TaskResponseDTO;
import com.bandwidth.taskservice.dto.TaskUpdateRequestDTO;
import com.bandwidth.taskservice.model.Task;
import com.bandwidth.taskservice.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService){this.taskService = taskService;}

    /****************************************************************************************/

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@RequestBody @Valid TaskCreateRequestDTO requestDTO){
        Long currentUserId = getCurrentUserId();
        requestDTO.setUserId(currentUserId);
        Task savedTask = taskService.createTask(requestDTO);
        TaskResponseDTO responseDTO = TaskResponseDTO.fromEntity(savedTask);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    /****************************************************************************************/

    @GetMapping("/user")
    public ResponseEntity<List<TaskResponseDTO>> getTasksByUser(){
        Long userId = getCurrentUserId();
        List<Task> tasks = taskService.getTasksByUserId(userId);
        List<TaskResponseDTO> responseDTOs = tasks.stream()
                .map(TaskResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }

    /****************************************************************************************/

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> updateTask(@PathVariable Long id, @RequestBody @Valid TaskUpdateRequestDTO requestDTO){
        Task updatedTask = taskService.updateTask(id, requestDTO);
        TaskResponseDTO responseDTO = TaskResponseDTO.fromEntity(updatedTask);
        return ResponseEntity.ok(responseDTO);
    }

    /****************************************************************************************/

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id){
        boolean wasDeleted = taskService.deleteTask(id);
        if (wasDeleted) {
            // Return 204 No Content for a successful deletion
            return ResponseEntity.noContent().build();
        } else {
            // Return 404 Not Found if the task ID doesn't exist
            return ResponseEntity.notFound().build();
        }
    }

    /****************************************************************************************/

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // The principal is the UserDetails object created in the JwtAuthenticationFilter,
            // and its username field holds the User ID string.
            String userIdStr = authentication.getName();
            try {
                return Long.valueOf(userIdStr);
            } catch (NumberFormatException e) {
                // Log or throw an error if the principal is not a valid Long
                throw new IllegalStateException("Authenticated principal is not a valid User ID.");
            }
        }
        throw new IllegalStateException("User not authenticated.");
    }

}
