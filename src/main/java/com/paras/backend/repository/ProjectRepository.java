package com.paras.backend.repository;

import com.paras.backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // Find all projects where this user is the owner OR a member
    @Query("""
        SELECT DISTINCT p FROM Project p
        LEFT JOIN p.members m
        WHERE p.owner.id = :userId OR m.user.id = :userId
    """)
    List<Project> findAllByUserId(@Param("userId") long userId);
}