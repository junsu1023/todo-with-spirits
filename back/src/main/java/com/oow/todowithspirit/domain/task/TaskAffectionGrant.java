package com.oow.todowithspirit.domain.task;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 할 일 완료로 대표 정령 호감도가 오른 기록.
 * 완료 취소 후 다시 완료해도 같은 할 일·날짜로는 호감도가 한 번만 오르도록 막는 용도이며, 완료 취소로 지우지 않는다.
 */
@Entity
@Table(
        name = "task_affection_grants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "completion_date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskAffectionGrant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "completion_date", nullable = false)
    private LocalDate completionDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static TaskAffectionGrant of(Long taskId, LocalDate completionDate) {
        TaskAffectionGrant grant = new TaskAffectionGrant();
        grant.taskId = taskId;
        grant.completionDate = completionDate;
        grant.createdAt = LocalDateTime.now();
        return grant;
    }
}