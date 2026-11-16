package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spirit_traits")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritTraitTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spirit_id", nullable = false)
    private Long spiritId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SpiritTrait trait;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public SpiritTraitTag(Long spiritId, SpiritTrait trait) {
        this.spiritId = spiritId;
        this.trait = trait;
        this.createdAt = LocalDateTime.now();
    }
}