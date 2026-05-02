package com.paras.backend.dto;

import com.paras.backend.model.enums.Priority;
import com.paras.backend.model.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id, String title, String description,
        TaskStatus status, Priority priority,
        LocalDate dueDate, boolean overdue,
        String assigneeName, String createdByName,
        LocalDateTime createdAt
) {}