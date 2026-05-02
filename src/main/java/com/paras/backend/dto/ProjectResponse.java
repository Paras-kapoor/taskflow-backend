package com.paras.backend.dto;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id, String name, String description,
        String ownerName, LocalDateTime createdAt,
        int memberCount, int taskCount
) {}