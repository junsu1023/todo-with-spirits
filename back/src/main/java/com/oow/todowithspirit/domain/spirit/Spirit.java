package com.oow.todowithspirit.domain.spirit;

import com.oow.todowithspirit.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spirit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spirit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "spirit_name", nullable = false, length = 50)
    private String spiritName = "아기 정령";

    @Column(nullable = false)
    private int stage = 1;

    @Column(nullable = false)
    private int exp = 0;

    @Column(name = "focus_exp", nullable = false)
    private int focusExp = 0;

    @Column(name = "vitality_exp", nullable = false)
    private int vitalityExp = 0;

    @Column(name = "persistence_exp", nullable = false)
    private int persistenceExp = 0;

    @Column(name = "creativity_exp", nullable = false)
    private int creativityExp = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Spirit(User user) {
        this.user = user;
    }

    // 경험치 획득 및 진화 비즈니스 로직 추가 영역
    public void addExp(int amount, GrowthType type) {
        this.exp += amount;
        switch (type) {
            case FOCUS -> this.focusExp += amount;
            case VITALITY -> this.vitalityExp += amount;
            case PERSISTENCE -> this.persistenceExp += amount;
            case CREATIVITY -> this.creativityExp += amount;
        }
        // 예시 만렙 경험치가 100일 때 진화 시스템 로직
        if (this.exp >= this.stage * 100) {
            this.stage++;
        }
        this.updatedAt = LocalDateTime.now();
    }
}