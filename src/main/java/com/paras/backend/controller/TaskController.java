package com.paras.backend.controller;

import com.paras.backend.dto.TaskRequest;
import com.paras.backend.dto.TaskResponse;
import com.paras.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskController {

     TaskService taskService;


    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksForProject(projectId, currentEmail()));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long projectId,
                                                @PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(projectId, taskId, currentEmail()));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long projectId,
                                                   @Valid @RequestBody TaskRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.createTask(projectId, req, currentEmail()));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long projectId,
                                                   @PathVariable Long taskId,
                                                   @Valid @RequestBody TaskRequest req) {
        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, req, currentEmail()));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long projectId,
                                           @PathVariable Long taskId) {
        taskService.deleteTask(projectId, taskId, currentEmail());
        return ResponseEntity.noContent().build();
    }
}
