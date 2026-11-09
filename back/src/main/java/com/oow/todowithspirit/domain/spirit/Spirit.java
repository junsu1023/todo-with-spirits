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

    private static final int MAX_AFFECTION = 100;
    private static final int AFFECTION_PER_LEVEL = 20;
    private static final int MAX_AFFECTION_LEVEL = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "spirit_name", nullable = false, length = 50)
    private String spiritName = "아기 정령";

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

    // 정령 종류 (spirit_species). 종류가 지정되지 않은 예전 정령은 null → 서비스에서 기본 종류로 취급
    @Column(name = "species_id")
    private Long speciesId;

    // 호감도 (0~100%). 할 일 완료로만 오르고 줄어들지 않음
    @Column(name = "affection", nullable = false, columnDefinition = "INT NOT NULL DEFAULT 0")
    private int affection = 0;

    // 사용자가 직접 지정하는 중요 표시
    @Column(name = "is_important", nullable = false, columnDefinition = "BOOLEAN NOT NULL DEFAULT FALSE")
    private boolean isImportant = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Spirit(User user) {
        this.user = user;
    }

    public Spirit(User user, String imageUrl) {
        this(user);
        this.imageUrl = imageUrl;
    }

    public Spirit(User user, String imageUrl, Long eggOptionId) {
        this(user, imageUrl);
        this.eggOptionId = eggOptionId;
    }

    // "정령 만나기"로 뽑힌 외형으로 개체를 생성 (외형이 속한 종류의 정령이 됨)
    public Spirit(User user, SpiritVariant variant) {
        this(user, variant.getImageUrl());
        this.variantId = variant.getId();
        this.speciesId = variant.getSpeciesId();
    }

    // 종류·외형 지정 (알로 생성되는 첫 정령 등 외형 없이 만들어진 경우)
    public void assignSpecies(Long speciesId, Long variantId) {
        this.speciesId = speciesId;
        if (variantId != null) {
            this.variantId = variantId;
        }
    }

    // 호감도 증가 (최대 100)
    public void increaseAffection(int amount) {
        this.affection = Math.min(MAX_AFFECTION, this.affection + amount);
        this.updatedAt = LocalDateTime.now();
    }

    // 호감도 단계 (1~5): 0~19=1, 20~39=2, 40~59=3, 60~79=4, 80~100=5
    public int getAffectionLevel() {
        return Math.min(MAX_AFFECTION_LEVEL, affection / AFFECTION_PER_LEVEL + 1);
    }

    public void updateImportant(boolean isImportant) {
        this.isImportant = isImportant;
        this.updatedAt = LocalDateTime.now();
    }

    // 경험치 획득 (성장 유형별 누적치)
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
        this.updatedAt = LocalDateTime.now();
    }

    public void updateName(String spiritName) {
        this.spiritName = spiritName;
    }
}
