package com.oow.todowithspirit.domain.spirit;

import com.oow.todowithspirit.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "spirits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spirit {

    // 성장기간(활동일 기준): 최소 4주(28일) ~ 최대 6주(42일). 구간이 대칭이라 평균이 자연히 5주(35일)가 됨
    private static final int MIN_GROWTH_DAYS = 28;
    private static final int MAX_GROWTH_DAYS = 42;

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

    @Column(name = "egg_option_id")
    private Long eggOptionId;

    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "target_growth_days", nullable = false)
    private int targetGrowthDays = MIN_GROWTH_DAYS + (MAX_GROWTH_DAYS - MIN_GROWTH_DAYS) / 2;

    @Column(name = "active_days_count", nullable = false)
    private int activeDaysCount = 0;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SpiritStatus status = SpiritStatus.GROWING;

    @Column(name = "matured_at")
    private LocalDateTime maturedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "career_growth_type", length = 20)
    private GrowthType careerGrowthType;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Spirit(User user) {
        this.user = user;
        this.targetGrowthDays = rollTargetGrowthDays();
    }

    public Spirit(User user, String imageUrl) {
        this(user);
        this.imageUrl = imageUrl;
    }

    public Spirit(User user, String imageUrl, Long eggOptionId) {
        this(user, imageUrl);
        this.eggOptionId = eggOptionId;
    }

    // "정령 만나기"로 뽑힌 외형으로 개체를 생성
    public Spirit(User user, SpiritVariant variant) {
        this(user, variant.getImageUrl());
        this.variantId = variant.getId();
    }

    // 경험치 획득 (진로 판단용 축적치. 더 이상 이 값만으로 성장 단계가 오르지 않음)
    public void addExp(int amount, GrowthType type) {
        this.exp += amount;
        switch (type) {
            case FOCUS -> this.focusExp += amount;
            case ENERGY -> this.energyExp += amount;
            case CONSISTENCY -> this.consistencyExp += amount;
            case CREATIVITY -> this.creativityExp += amount;
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
        // 완료를 취소해도 이미 쌓인 생활 기록(활동일)은 되돌리지 않음 (퇴보 없음)
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 오늘(activityDate) 생활 기록이 처음 생겼음을 반영한다.
     * 같은 날짜가 이미 반영된 경우 아무 일도 하지 않는다. 호출 전 해당 날짜의 중복 여부는 호출자가 판단한다.
     * @return 이번 기록으로 성체가 되었는지 여부
     */
    public boolean recordActiveDay(LocalDate activityDate) {
        if (status != SpiritStatus.GROWING) {
            return false;
        }

        this.activeDaysCount++;
        if (lastActiveDate == null || activityDate.isAfter(lastActiveDate)) {
            this.lastActiveDate = activityDate;
        }
        this.updatedAt = LocalDateTime.now();

        if (activeDaysCount >= targetGrowthDays) {
            matureIntoAdult();
            return true;
        }
        return false;
    }

    private void matureIntoAdult() {
        this.status = SpiritStatus.ADULT;
        this.maturedAt = LocalDateTime.now();
        this.careerGrowthType = dominantGrowthType();
        this.stage++;
    }

    private GrowthType dominantGrowthType() {
        int max = Math.max(Math.max(focusExp, energyExp), Math.max(consistencyExp, creativityExp));
        if (max == focusExp) {
            return GrowthType.FOCUS;
        } else if (max == energyExp) {
            return GrowthType.ENERGY;
        } else if (max == consistencyExp) {
            return GrowthType.CONSISTENCY;
        }
        return GrowthType.CREATIVITY;
    }

    public void updateName(String spiritName) {
        this.spiritName = spiritName;
    }

    private static int rollTargetGrowthDays() {
        return ThreadLocalRandom.current().nextInt(MIN_GROWTH_DAYS, MAX_GROWTH_DAYS + 1);
    }
}
