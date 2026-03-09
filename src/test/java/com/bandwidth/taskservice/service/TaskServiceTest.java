package com.bandwidth.taskservice.service;

import com.bandwidth.taskservice.dto.TaskCreateRequestDTO;
import com.bandwidth.taskservice.dto.TaskUpdateRequestDTO;
import com.bandwidth.taskservice.model.Priority;
import com.bandwidth.taskservice.model.Task;
import com.bandwidth.taskservice.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    // --- CREATE TASK ---

    @Test
    @DisplayName("createTask - Success")
    void createTask_Success() {
        TaskCreateRequestDTO request = new TaskCreateRequestDTO("Test Task", Priority.MEDIUM, 1L);
        Task savedTask = new Task();
        savedTask.setId(100L);
        savedTask.setText("Test Task");

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        Task result = taskService.createTask(request);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask - Verify DTO to Entity Mapping")
    void createTask_MappingDetails() {
        TaskCreateRequestDTO request = new TaskCreateRequestDTO("Deep Test", Priority.HIGH, 5L);

        // Use an ArgumentCaptor to see what actually goes into the save() method
        org.mockito.ArgumentCaptor<Task> taskCaptor = org.mockito.ArgumentCaptor.forClass(Task.class);

        taskService.createTask(request);

        verify(taskRepository).save(taskCaptor.capture());
        Task capturedTask = taskCaptor.getValue();

        assertEquals("Deep Test", capturedTask.getText());
        assertEquals(Priority.HIGH, capturedTask.getPriority());
        assertEquals(5L, capturedTask.getUserId());
    }

    // --- GET TASKS ---

    @Test
    @DisplayName("getTasksByUserId - Success")
    void getTasksByUserId_Success() {
        when(taskRepository.findByUserId(1L)).thenReturn(List.of(new Task(), new Task()));

        List<Task> results = taskService.getTasksByUserId(1L);

        assertEquals(2, results.size());
        verify(taskRepository).findByUserId(1L);
    }

    @Test
    @DisplayName("getTasksByUserId - Returns empty list when user has no tasks")
    void getTasksByUserId_Empty() {
        when(taskRepository.findByUserId(99L)).thenReturn(List.of());

        List<Task> results = taskService.getTasksByUserId(99L);

        assertTrue(results.isEmpty());
        verify(taskRepository).findByUserId(99L);
    }

    // --- UPDATE TASK ---

    @Test
    @DisplayName("updateTask - Success with all fields")
    void updateTask_Success() {
        Long taskId = 1L;
        Task existingTask = new Task();
        existingTask.setId(taskId);
        existingTask.setText("Old text");
        existingTask.setPriority(Priority.LOW);
        existingTask.setCompleted(false);

        TaskUpdateRequestDTO updateDTO = new TaskUpdateRequestDTO("New text", Priority.HIGH, true);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.updateTask(taskId, updateDTO);

        assertEquals("New text", result.getText());
        assertEquals(Priority.HIGH, result.getPriority());
        assertTrue(result.isCompleted());
    }

    @Test
    @DisplayName("updateTask - Success with null fields (Partial Update)")
    void updateTask_PartialUpdate() {
        Long taskId = 1L;
        Task existingTask = new Task();
        existingTask.setText("Keep this");
        existingTask.setPriority(Priority.LOW);

        // Only updating the text, leaving others null
        TaskUpdateRequestDTO updateDTO = new TaskUpdateRequestDTO("New text", null, null);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        Task result = taskService.updateTask(taskId, updateDTO);

        assertEquals("New text", result.getText());
        assertEquals(Priority.LOW, result.getPriority()); // Should not have changed
        verify(taskRepository).save(existingTask);
    }

    @Test
    @DisplayName("updateTask - Throws EntityNotFoundException")
    void updateTask_NotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                taskService.updateTask(1L, new TaskUpdateRequestDTO())
        );
    }

    // --- DELETE TASK ---

    @Test
    @DisplayName("deleteTask - Returns true when exists")
    void deleteTask_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(new Task()));

        boolean deleted = taskService.deleteTask(1L);

        assertTrue(deleted);
        verify(taskRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTask - Returns false when not found")
    void deleteTask_NotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        boolean deleted = taskService.deleteTask(1L);

        assertFalse(deleted);
        verify(taskRepository, never()).deleteById(anyLong());
    }

}