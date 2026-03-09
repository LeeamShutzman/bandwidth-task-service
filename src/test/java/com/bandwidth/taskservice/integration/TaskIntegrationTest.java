package com.bandwidth.taskservice.integration;

import com.bandwidth.taskservice.dto.TaskCreateRequestDTO;
import com.bandwidth.taskservice.dto.TaskResponseDTO;
import com.bandwidth.taskservice.model.Priority;
import com.bandwidth.taskservice.repository.TaskRepository;
import com.bandwidth.taskservice.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "app.allowed.origins=http://localhost:8081"
})
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService; // Still mock the service that validates real tokens

    // Once you add Kafka, you will add a @MockBean here for your TaskKafkaConsumer/Producer

    @Test
    @DisplayName("Scenario: Create a task and then retrieve it for the user")
    @WithMockUser(username = "100") // Simulate User ID 100
    void createAndGetTask_IntegrationScenario() throws Exception {
        // 1. Arrange: Prepare a request
        TaskCreateRequestDTO request = new TaskCreateRequestDTO("Finish Integration Test", Priority.HIGH, 100L);

        // 2. Act: Create the task via the API
        mockMvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Finish Integration Test"))
                .andExpect(jsonPath("$.userId").value(100));

        // 3. Act & Assert: Retrieve tasks for user 100
        mockMvc.perform(get("/api/v1/tasks/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Finish Integration Test"));

        // 4. Assert: Verify database state
        assertEquals(1, taskRepository.count());
    }

    @Test
    @DisplayName("Scenario: Update a task's status and verify persistence")
    @WithMockUser(username = "100")
    void updateTask_IntegrationScenario() throws Exception {
        // 1. Pre-insert a task into the real H2 database
        var task = new com.bandwidth.taskservice.model.Task();
        task.setUserId(100L);
        task.setText("Old Task");
        task.setPriority(Priority.LOW);
        task = taskRepository.save(task);

        // 2. Act: Update via API
        var updateRequest = new com.bandwidth.taskservice.dto.TaskUpdateRequestDTO("New Task", Priority.MEDIUM, true);

        mockMvc.perform(put("/api/v1/tasks/" + task.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));

        // 3. Assert: Database matches the update
        var updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(Priority.MEDIUM, updatedTask.getPriority());
    }
}