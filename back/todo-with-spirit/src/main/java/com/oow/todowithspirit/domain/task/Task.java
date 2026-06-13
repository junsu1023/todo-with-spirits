package com.oow.todowithspirit.domain.task;

import com.oow.todowithspirit.domain.spirit.GrowthType;
import com.oow.todowithspirit.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 50)
    private String category;

    @Column(name = "task_time")
    private LocalTime taskTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_rule", nullable = false)
    RepeatType repeatRule = RepeatType.NONE;

    @Column(nullable = false)
    private int power = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "power_type", nullable = false)
    private GrowthType powerType;

    @Column(name = "is_completed", nullable = false)
    private boolean isCompleted = false;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Builder
    public Task(User user, TaskType type, String title, String category, LocalTime taskTime,
                RepeatType repeatRule, int power, GrowthType powerType, LocalDate taskDate) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.category = category;
        this.taskTime = taskTime;
        this.repeatRule = repeatRule;
        this.power = power;
        this.powerType = powerType;
        this.taskDate = taskDate;
    }

    public void completeTask() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }

    public void undoCompleteTask() {
        this.isCompleted = false;
        this.completedAt = null;
    }
}