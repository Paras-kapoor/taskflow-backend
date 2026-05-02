package com.paras.backend.dto;

import com.paras.backend.model.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record AddMemberRequest(
        @NotNull Long userId,
        @NotNull ProjectRole role
) {}
