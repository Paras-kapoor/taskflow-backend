package com.paras.backend.service;

import com.paras.backend.dto.ProjectRequest;
import com.paras.backend.dto.ProjectResponse;
import com.paras.backend.model.Project;
import com.paras.backend.model.ProjectMember;
import com.paras.backend.model.User;
import com.paras.backend.model.enums.ProjectRole;
import com.paras.backend.repository.ProjectMemberRepository;
import com.paras.backend.repository.ProjectRepository;
import com.paras.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class ProjectService {

     ProjectRepository projectRepository;
     ProjectMemberRepository memberRepository;
     UserRepository userRepository;

    public ProjectResponse createProject(ProjectRequest req, String ownerEmail) {
        User owner = userRepository.findByEmail(ownerEmail).orElseThrow();

        Project project = Project.builder()
                .name(req.name())
                .description(req.description())
                .owner(owner)
                .build();
        project = projectRepository.save(project);

        // Owner is automatically an ADMIN member
        ProjectMember ownerMember = ProjectMember.builder()
                .project(project)
                .user(owner)
                .role(ProjectRole.ADMIN)
                .build();
        memberRepository.save(ownerMember);

        return toResponse(project);
    }

    public void addMember(long projectId, long userId, ProjectRole role, String requesterEmail) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        User requester = userRepository.findByEmail(requesterEmail).orElseThrow();

        // Only ADMINs can add members
        ProjectMember requesterMembership = memberRepository
                .findByProjectIdAndUserId(projectId, requester.getId())
                .orElseThrow(() -> new AccessDeniedException("Not a member of this project"));

        if (requesterMembership.getRole() != ProjectRole.ADMIN) {
            throw new AccessDeniedException("Only ADMINs can add members");
        }

        if (memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new RuntimeException("User is already a member");
        }

        User newMember = userRepository.findById(userId).orElseThrow();
        memberRepository.save(ProjectMember.builder()
                .project(project).user(newMember).role(role).build());
    }

    public List<ProjectResponse> getProjectsForUser(String email) {
        User user = getUserByEmail(email);
        return projectRepository.findAllByUserId(user.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long projectId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        Project project = getProjectOrThrow(projectId);
        assertMember(projectId, requester.getId());   // only members can view
        return toResponse(project);
    }

    public ProjectResponse updateProject(Long projectId, ProjectRequest req, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        Project project = getProjectOrThrow(projectId);
        assertAdmin(projectId, requester.getId());    // only ADMIN can update

        project.setName(req.name());
        project.setDescription(req.description());
        return toResponse(projectRepository.save(project));
    }

    public void deleteProject(Long projectId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        getProjectOrThrow(projectId);                 // ensure it exists
        assertAdmin(projectId, requester.getId());    // only ADMIN can delete
        projectRepository.deleteById(projectId);
    }

    public void addMember(Long projectId, Long userId, ProjectRole role, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        getProjectOrThrow(projectId);
        assertAdmin(projectId, requester.getId());    // only ADMIN can add members

        if (memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new RuntimeException("User is already a member of this project");
        }

        User newMember = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Project project = getProjectOrThrow(projectId);

        memberRepository.save(ProjectMember.builder()
                .project(project)
                .user(newMember)
                .role(role)
                .build());
    }

    public void removeMember(Long projectId, Long userId, String requesterEmail) {
        User requester = getUserByEmail(requesterEmail);
        assertAdmin(projectId, requester.getId());

        ProjectMember member = memberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new NoSuchElementException("Member not found"));
        memberRepository.delete(member);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + email));
    }

    private Project getProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Project not found: " + id));
    }

    private void assertMember(Long projectId, Long userId) {
        if (!memberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new AccessDeniedException("You are not a member of this project");
        }
    }

    private void assertAdmin(Long projectId, Long userId) {
        ProjectMember membership = memberRepository
                .findByProjectIdAndUserId(projectId, userId)
                .orElseThrow(() -> new AccessDeniedException("You are not a member of this project"));
        if (membership.getRole() != ProjectRole.ADMIN) {
            throw new AccessDeniedException("Only project ADMINs can perform this action");
        }
    }

    // Maps a Project entity → ProjectResponse DTO
    private ProjectResponse toResponse(Project p) {
        // Use size of collections; handle lazy-loaded nulls safely
        int memberCount = p.getMembers() != null ? p.getMembers().size() : 0;
        int taskCount   = p.getTasks()   != null ? p.getTasks().size()   : 0;
        return new ProjectResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getOwner().getName(),
                p.getCreatedAt(),
                memberCount,
                taskCount
        );
    }

}
