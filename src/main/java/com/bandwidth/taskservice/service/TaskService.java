package com.bandwidth.taskservice.service;

import com.bandwidth.taskservice.dto.TaskCreateRequestDTO;
import com.bandwidth.taskservice.dto.TaskUpdateRequestDTO;
import com.bandwidth.taskservice.model.Task;
import com.bandwidth.taskservice.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository){this.taskRepository = taskRepository;}

    /****************************************************************************************/

    @Transactional
    public Task createTask(TaskCreateRequestDTO requestDTO) {
        Task task = convertDtoToEntity(requestDTO);
        return taskRepository.save(task);
    }

    /****************************************************************************************/

    public List<Task> getTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    /****************************************************************************************/

    @Transactional
    public Task updateTask(Long taskId, TaskUpdateRequestDTO requestDTO) {
        Task taskToUpdate = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));
        applyUpdatesToEntity(taskToUpdate, requestDTO);
        return taskRepository.save(taskToUpdate);
    }

    /****************************************************************************************/

    @Transactional
    public boolean deleteTask(Long taskId) {
        Optional<Task> task = taskRepository.findById(taskId);
        if (task.isPresent()) {
            taskRepository.deleteById(taskId);
            return true;
        }
        return false;
    }

    /****************************************************************************************/

    /** Utils **/

    private Task convertDtoToEntity(TaskCreateRequestDTO dto) {
        Task task = new Task();
        task.setUserId(dto.getUserId());
        task.setText(dto.getText());
        task.setPriority(dto.getPriority());
        return task;
    }

    private void applyUpdatesToEntity(Task task, TaskUpdateRequestDTO dto) {
        if (dto.getText() != null) {
            task.setText(dto.getText());
        }
        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        }
        if (dto.getCompleted() != null) {
            task.setCompleted(dto.getCompleted());
        }
    }



}
