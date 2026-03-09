package com.bandwidth.taskservice.controller;

import com.bandwidth.taskservice.dto.TaskCreateRequestDTO;
import com.bandwidth.taskservice.dto.TaskUpdateRequestDTO;
import com.bandwidth.taskservice.model.Priority;
import com.bandwidth.taskservice.model.Task;
import com.bandwidth.taskservice.security.JwtService;
import com.bandwidth.taskservice.service.TaskService;
import com.bandwidth.taskservice.config.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "app.allowed.origins=http://localhost:8081"
})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtService jwtService; // Required to load SecurityConfig correctly

    // --- CREATE TASK TESTS ---

    @Test
    @DisplayName("POST / - Success")
    @WithMockUser(username = "123") // getName() returns "123", converted to Long 123
    void createTask_Success() throws Exception {
        TaskCreateRequestDTO request = new TaskCreateRequestDTO("My Task", Priority.HIGH, 123L);
        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setUserId(123L);
        savedTask.setText("My Task");
        savedTask.setPriority(Priority.HIGH);

        when(taskService.createTask(any(TaskCreateRequestDTO.class))).thenReturn(savedTask);

        mockMvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("My Task"));
    }

    @Test
    @DisplayName("POST / - Validation Failure (400)")
    @WithMockUser(username = "123")
    void createTask_ValidationFailure() throws Exception {
        // Assuming @NotBlank is on title in TaskCreateRequestDTO
        TaskCreateRequestDTO invalidRequest = new TaskCreateRequestDTO("", Priority.HIGH, 123L);

        mockMvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    // --- GET TASKS TESTS ---

    @Test
    @DisplayName("GET /user - Success")
    @WithMockUser(username = "456")
    void getTasksByUser_Success() throws Exception {
        Task task = new Task();
        task.setId(1L);
        task.setUserId(456L);
        task.setText("My Task");
        task.setPriority(Priority.LOW);
        when(taskService.getTasksByUserId(456L)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/v1/tasks/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(456));
    }

    @Test
    @DisplayName("GET /user - Returns empty array when no tasks exist")
    @WithMockUser(username = "123")
    void getTasksByUser_EmptyList() throws Exception {
        when(taskService.getTasksByUserId(123L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/tasks/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // Explicitly check array length
    }

    // --- UPDATE TASK TESTS ---

    @Test
    @DisplayName("PUT /{id} - Success")
    @WithMockUser(username = "123")
    void updateTask_Success() throws Exception {
        TaskUpdateRequestDTO updateRequest = new TaskUpdateRequestDTO("Updated text", Priority.HIGH, false);
        Task updatedTask = new Task();
        updatedTask.setUserId(123L);
        updatedTask.setText("Updated text");
        updatedTask.setPriority(Priority.HIGH);

        when(taskService.updateTask(eq(1L), any(TaskUpdateRequestDTO.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/v1/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Updated text"));
    }

    @Test
    @DisplayName("PUT /{id} - Returns 404 when task does not exist")
    @WithMockUser(username = "123")
    void updateTask_NotFound() throws Exception {
        TaskUpdateRequestDTO updateRequest = new TaskUpdateRequestDTO("Updated text", Priority.HIGH, false);

        // Simulate the service throwing an exception (assuming you have a GlobalExceptionHandler)
        when(taskService.updateTask(eq(99L), any(TaskUpdateRequestDTO.class)))
                .thenThrow(new RuntimeException("Task not found"));

        mockMvc.perform(put("/api/v1/tasks/99")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    // --- DELETE TASK TESTS ---

    @Test
    @DisplayName("DELETE /{id} - Success (204)")
    @WithMockUser(username = "123")
    void deleteTask_Success() throws Exception {
        when(taskService.deleteTask(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/tasks/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /{id} - Not Found (404)")
    @WithMockUser(username = "123")
    void deleteTask_NotFound() throws Exception {
        when(taskService.deleteTask(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/v1/tasks/99").with(csrf()))
                .andExpect(status().isNotFound());
    }

    // --- SECURITY & PRIVATE METHOD COVERAGE ---

    @Test
    @DisplayName("getCurrentUserId - Throws 500 when username is not numeric")
    @WithMockUser(username = "not-a-number")
    void getCurrentUserId_NonNumericThrowsError() throws Exception {
        // This triggers the try-catch block in your private method
        mockMvc.perform(get("/api/v1/tasks/user"))
                .andExpect(status().isInternalServerError());
        // Note: Spring usually returns 500 for IllegalStateException
    }

    @Test
    @DisplayName("getCurrentUserId - Throws 403 when unauthenticated")
    @WithMockUser
    void getCurrentUserId_Unauthenticated() throws Exception {
        // This triggers the "User not authenticated" exception
        SecurityContextHolder.getContext().getAuthentication().setAuthenticated(false);
        mockMvc.perform(get("/api/v1/tasks/user"))
                .andExpect(status().isForbidden());
        // Security filter usually catches this before the method,
        // but this ensures the path is blocked.
    }

    @Test
    @DisplayName("getCurrentUserId - Throws 500 when SecurityContext is empty")
    @WithMockUser // gets cleared in the function
    void getCurrentUserId_NullAuthentication() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/api/v1/tasks/user"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Any Request - Returns 401/403 when no user is present")
    void anyRequest_AnonymousUser() throws Exception {
        // No @WithMockUser annotation here
        mockMvc.perform(get("/api/v1/tasks/user"))
                .andExpect(status().isForbidden());
    }
}