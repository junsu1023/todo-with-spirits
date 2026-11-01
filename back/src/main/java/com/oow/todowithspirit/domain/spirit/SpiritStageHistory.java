package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spirit_stage_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritStageHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spirit_id", nullable = false)
    private Long spiritId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "from_stage", nullable = false)
    private int fromStage;

    @Column(name = "to_stage", nullable = false)
    private int toStage;

    @Column(name = "evolved_at")
    private LocalDateTime evolvedAt;

    public static SpiritStageHistory of(Long spiritId, Long userId, int fromStage, int toStage) {
        SpiritStageHistory history = new SpiritStageHistory();
        history.spiritId = spiritId;
        history.userId = userId;
        history.fromStage = fromStage;
        history.toStage = toStage;
        history.evolvedAt = LocalDateTime.now();
        return history;
    }
}