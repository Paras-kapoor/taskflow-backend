package com.paras.backend.dto;

import com.paras.backend.model.enums.Priority;
import com.paras.backend.model.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank String title,
        String description,
        TaskStatus status,
        Priority priority,
        LocalDate dueDate,
        Long assigneeId   // optional
) {}