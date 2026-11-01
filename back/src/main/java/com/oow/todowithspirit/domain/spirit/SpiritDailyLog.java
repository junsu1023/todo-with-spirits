package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "spirit_daily_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritDailyLog {

    // 컨텐츠 생성 로직이 정해지기 전까지 사용하는 기본 문구
    private static final String DEFAULT_CONTENT = "오늘도 정령과 함께 하루를 기록했어요.";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spirit_id", nullable = false)
    private Long spiritId;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static SpiritDailyLog of(Long spiritId, LocalDate logDate) {
        SpiritDailyLog log = new SpiritDailyLog();
        log.spiritId = spiritId;
        log.logDate = logDate;
        log.content = DEFAULT_CONTENT;
        log.createdAt = LocalDateTime.now();
        return log;
    }
}
