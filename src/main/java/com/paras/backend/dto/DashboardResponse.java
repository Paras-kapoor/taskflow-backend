package com.paras.backend.dto;

import java.util.List;
import java.util.Map;

public record DashboardResponse(
        int totalProjects,
        int totalTasks,
        int overdueTasks,
        Map<String, Long> tasksByStatus,
        List<TaskResponse> myTasks
) {}
