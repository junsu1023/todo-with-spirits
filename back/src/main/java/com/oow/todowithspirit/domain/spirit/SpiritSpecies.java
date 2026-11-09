package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 정령 종류 (정령도감의 한 칸).
 * 종류마다 외형(spirit_variants)을 여러 개(최대 6개) 가지며, 사용자는 종류당 정령을 1마리만 보유한다.
 */
@Entity
@Table(name = "spirit_species")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritSpecies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "species_name", nullable = false, length = 50)
    private String speciesName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "asset_key", nullable = false, length = 100)
    private String assetKey;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_file_name", nullable = false, length = 150)
    private String imageFileName;
}
