package com.bandwidth.taskservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "task")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 512)
    private String text;

    @Enumerated(EnumType.STRING) // Saves the Enum name (e.g., "HIGH") as a String
    @Column(nullable = false, length = 10)
    private Priority priority;

    /* unused for now */

    @Column(name = "is_completed", nullable = false)
    private boolean completed = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
