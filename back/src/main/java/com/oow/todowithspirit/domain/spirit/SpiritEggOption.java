package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spirit_egg_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritEggOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "egg_name", nullable = false, length = 50)
    private String eggName;

    @Column
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_tutorial", nullable = false)
    private boolean isTutorial;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}