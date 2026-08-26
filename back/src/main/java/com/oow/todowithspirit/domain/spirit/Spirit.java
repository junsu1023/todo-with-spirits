package com.oow.todowithspirit.domain.spirit;

import com.oow.todowithspirit.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spirits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spirit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "spirit_name", nullable = false, length = 50)
    private String spiritName = "아기 정령";

    @Column(nullable = false)
    private int stage = 1;

    @Column(nullable = false)
    private int exp = 0;

    @Column(name = "focus_exp", nullable = false)
    private int focusExp = 0;

    @Column(name = "energy_exp", nullable = false)
    private int energyExp = 0;

    @Column(name = "consistency_exp", nullable = false)
    private int consistencyExp = 0;

    @Column(name = "creativity_exp", nullable = false)
    private int creativityExp = 0;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Spirit(User user) {
        this.user = user;
    }

    public Spirit(User user, String imageUrl) {
        this.user = user;
        this.imageUrl = imageUrl;
    }

    // 경험치 획득 및 진화
    public void addExp(int amount, GrowthType type) {
        this.exp += amount;
        switch (type) {
            case FOCUS -> this.focusExp += amount;
            case ENERGY -> this.energyExp += amount;
            case CONSISTENCY -> this.consistencyExp += amount;
            case CREATIVITY -> this.creativityExp += amount;
        }
        // 만렙 경험치가 100일 때 진화
        if (this.exp >= this.stage * 100) {
            this.stage++;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // 완료 취소 시 경험치 감소
    public void decreaseExp(int amount, GrowthType type) {
        this.exp = Math.max(0, this.exp - amount);
        switch (type) {
            case FOCUS -> this.focusExp = Math.max(0, this.focusExp - amount);
            case ENERGY -> this.energyExp = Math.max(0, this.energyExp - amount);
            case CONSISTENCY -> this.consistencyExp = Math.max(0, this.consistencyExp - amount);
            case CREATIVITY -> this.creativityExp = Math.max(0, this.creativityExp - amount);
        }

        // (선택) 만약 경험치가 깎여서 이전 레벨로 강등(Level Down)되는 로직이 필요하다면 여기에 추가
        // 일반적으로 게임 기획상 레벨 강등은 스트레스를 주어 구현하지 않는 경우가 많습니다.

        this.updatedAt = LocalDateTime.now();
    }
}