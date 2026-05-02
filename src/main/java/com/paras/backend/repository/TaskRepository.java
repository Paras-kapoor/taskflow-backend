package com.paras.backend.repository;

import com.paras.backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllByProjectId(long projectId);
    List<Task> findAllByAssigneeId(long userId);

    // For dashboard — overdue tasks (past due date, not yet DONE)
    @Query("""
        SELECT t FROM Task t
        WHERE t.assignee.id = :userId
        AND t.dueDate < :today
        AND t.status != 'DONE'
    """)
    List<Task> findOverdueTasksForUser(@Param("userId") long userId,
                                       @Param("today") LocalDate today);

    // Count by status for dashboard charts
    @Query("SELECT t.status, COUNT(t) FROM Task t WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> countByStatusForProject(@Param("projectId") long projectId);
}