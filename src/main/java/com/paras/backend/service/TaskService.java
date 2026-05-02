package com.paras.backend.service;

import com.paras.backend.dto.DashboardResponse;
import com.paras.backend.dto.TaskRequest;
import com.paras.backend.dto.TaskResponse;
import com.paras.backend.model.Project;
import com.paras.backend.model.ProjectMember;
import com.paras.backend.model.Task;
import com.paras.backend.model.User;
import com.paras.backend.model.enums.Priority;
import com.paras.backend.model.enums.ProjectRole;
import com.paras.backend.model.enums.TaskStatus;
import com.paras.backend.repository.ProjectMemberRepository;
import com.paras.backend.repository.ProjectRepository;
import com.paras.backend.repository.TaskRepository;
import com.paras.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class TaskService {

     TaskRepository taskRepository;
     ProjectRepository projectRepository;
     UserRepository userRepository;
     ProjectMemberRepository memberRepository;

    public TaskResponse createTask(Long projectId, TaskRequest req, String creatorEmail) {
        User creator = getUserByEmail(creatorEmail);
        Project project = getProjectOrThrow(projectId);
        assertMember(projectId, creator.getId());     // must be a member to create tasks

        User assignee = null;
        if (req.assigneeId() != null) {
            assignee = userRepository.findById(req.assigneeId())
                    .orElseThrow(() -> new NoSuchElementException("Assignee not found"));
            // Assignee must also be a member of the project
            if (!memberRepository.existsByProjectIdAndUserId(projectId, assignee.getId())) {
                throw new RuntimeException("Assignee is not a member of this project");
            }
        }

        Task task = Task.builder()
                .title(req.title())
                .description(req.description())
                .status(req.status() != null ? req.status() : TaskStatus.TODO)
                .priority(req.priority() != null ? req.priority() : Priority.MEDIUM)
                .dueDate(req.dueDate())
                .project(project)
                .assignee(assignee)
                .createdBy(creator)
                .build();

        return toResponse(taskRepository.save(task));
    }

    public List<TaskResponse> getTasksForProject(Long projectId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        assertMember(projectId, requester.getId());   // only members can see tasks
        return taskRepository.findAllByProjectId(projectId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long projectId, Long taskId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        assertMember(projectId, requester.getId());
        Task task = getTaskOrThrow(taskId);
        return toResponse(task);
    }

    public TaskResponse updateTask(Long projectId, Long taskId,
                                   TaskRequest req, String updaterEmail) {
        User updater = getUserByEmail(updaterEmail);
        Task task = getTaskOrThrow(taskId);

        // Assignee OR project ADMIN can update a task
        boolean isAssignee = task.getAssignee() != null &&
                task.getAssignee().getId().equals(updater.getId());
        boolean isAdmin    = memberRepository
                .findByProjectIdAndUserId(projectId, updater.getId())
                .map(m -> m.getRole() == ProjectRole.ADMIN)
                .orElse(false);

        if (!isAssignee && !isAdmin) {
            throw new AccessDeniedException("Only the assignee or a project ADMIN can update this task");
        }

        task.setTitle(req.title());
        task.setDescription(req.description());
        if (req.status()   != null) task.setStatus(req.status());
        if (req.priority() != null) task.setPriority(req.priority());
        task.setDueDate(req.dueDate());

        if (req.assigneeId() != null) {
            User newAssignee = userRepository.findById(req.assigneeId())
                    .orElseThrow(() -> new NoSuchElementException("Assignee not found"));
            task.setAssignee(newAssignee);
        }

        return toResponse(taskRepository.save(task));
    }

    public void deleteTask(Long projectId, Long taskId, String deleterEmail) {
        User deleter = getUserByEmail(deleterEmail);
        getTaskOrThrow(taskId);                       // ensure task exists
        assertAdmin(projectId, deleter.getId());      // only ADMIN can delete tasks
        taskRepository.deleteById(taskId);
    }

    public DashboardResponse getDashboard(String email) {
        User user = getUserByEmail(email);

        List<Project> myProjects = projectRepository.findAllByUserId(user.getId());
        List<Task> myTasks       = taskRepository.findAllByAssigneeId(user.getId());
        List<Task> overdueTasks  = taskRepository.findOverdueTasksForUser(
                user.getId(), LocalDate.now());

        // Group tasks by status → count each group
        Map<String, Long> tasksByStatus = myTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus().name(),
                        Collectors.counting()
                ));

        return new DashboardResponse(
                myProjects.size(),
                myTasks.size(),
                overdueTasks.size(),
                tasksByStatus,
                myTasks.stream().map(this::toResponse).collect(Collectors.toList())
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + email));
    }

    private Project getProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + id));
    }

    private Task getTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found: " + id));
    }

    private void assertMember(Long projectId, Long userId) {
        if (!memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }

    private void assertAdmin(Long projectId, Long userId) {
        ProjectMember m = memberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project"));
        if (m.getRole() != ProjectRole.ADMIN) {
            throw new AccessDeniedException("Only project ADMINs can perform this action");
        }
    }

    // Maps a Task entity → TaskResponse DTO
    private TaskResponse toResponse(Task t) {
        boolean overdue = t.getDueDate() != null
                && t.getDueDate().isBefore(LocalDate.now())
                && t.getStatus() != TaskStatus.DONE;
        return new TaskResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getDueDate(),
                overdue,
                t.getAssignee()  != null ? t.getAssignee().getName()  : null,
                t.getCreatedBy() != null ? t.getCreatedBy().getName() : null,
                t.getCreatedAt()
        );
    }
}
