package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spirit_variants")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "variant_name", nullable = false, length = 50)
    private String variantName;

    @Enumerated(EnumType.STRING)
    @Column(name = "unlock_metric", nullable = false, length = 30)
    private UnlockMetric unlockMetric;

    @Column(name = "unlock_threshold", nullable = false)
    private int unlockThreshold;

    @Column(nullable = false)
    private int weight = 1;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "asset_key", nullable = false, length = 100)
    private String assetKey;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_file_name", length = 150)
    private String imageFileName;

    public boolean isAlwaysUnlocked() {
        return unlockMetric == UnlockMetric.DEFAULT;
    }
}
