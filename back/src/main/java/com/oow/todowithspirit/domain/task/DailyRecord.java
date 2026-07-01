package com.oow.todowithspirit.domain.task;

import com.oow.todowithspirit.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_records", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "record_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "total_power", nullable = false)
    private int totalPower = 0;

    @Column(name = "completion_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal completionRate = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String interpretation;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DailyRecord(User user, LocalDate recordDate, int totalPower, BigDecimal completionRate, String interpretation) {
        this.user = user;
        this.recordDate = recordDate;
        this.totalPower = totalPower;
        this.completionRate = completionRate;
        this.interpretation = interpretation;
    }
}