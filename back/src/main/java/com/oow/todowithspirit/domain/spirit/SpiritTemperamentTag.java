package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spirit_temperaments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritTemperamentTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spirit_id", nullable = false)
    private Long spiritId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SpiritTemperament temperament;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public SpiritTemperamentTag(Long spiritId, SpiritTemperament temperament) {
        this.spiritId = spiritId;
        this.temperament = temperament;
        this.createdAt = LocalDateTime.now();
    }
}