package com.oow.todowithspirit.domain.task;

import com.oow.todowithspirit.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "task_postponements")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskPostponement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private CategoryType category;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 20)
    private TaskType taskType;

    @Column(name = "original_date", nullable = false)
    private LocalDate originalDate;

    @Column(name = "postponed_date", nullable = false)
    private LocalDate postponedDate;

    @Column(name = "postponed_time")
    private LocalTime postponedTime;

    public static TaskPostponement create(Task task, LocalDate originalDate, LocalDate postponedDate, LocalTime postponedTime) {
        TaskPostponement postponement = new TaskPostponement();
        postponement.taskId = task.getId();
        postponement.userId = task.getUser().getId();
        postponement.category = task.getCategory();
        postponement.taskType = task.getTaskType();
        postponement.originalDate = originalDate;
        postponement.postponedDate = postponedDate;
        postponement.postponedTime = postponedTime;
        return postponement;
    }
}