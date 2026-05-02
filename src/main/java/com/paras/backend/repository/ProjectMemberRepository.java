package com.paras.backend.repository;

import com.paras.backend.model.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    Optional<ProjectMember> findByProjectIdAndUserId(long projectId, long userId);
    boolean existsByProjectIdAndUserId(long projectId, long userId);
    List<ProjectMember> findAllByProjectId(long projectId);
}
