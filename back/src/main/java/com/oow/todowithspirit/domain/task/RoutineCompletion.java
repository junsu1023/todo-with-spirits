package com.oow.todowithspirit.domain.task;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "routine_completions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "completion_date"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutineCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "completion_date", nullable = false)
    private LocalDate completionDate;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    public static RoutineCompletion of(Task task, LocalDate completionDate) {
        RoutineCompletion rc = new RoutineCompletion();
        rc.task = task;
        rc.completionDate = completionDate;
        rc.completedAt = LocalDateTime.now();
        return rc;
    }
}