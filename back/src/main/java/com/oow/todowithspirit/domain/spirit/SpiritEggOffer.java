package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spirit_egg_offers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritEggOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "egg_option_id", nullable = false)
    private Long eggOptionId;

    @Column(name = "is_selected", nullable = false)
    private boolean selected;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public static SpiritEggOffer of(Long userId, Long eggOptionId) {
        SpiritEggOffer offer = new SpiritEggOffer();
        offer.userId = userId;
        offer.eggOptionId = eggOptionId;
        offer.createdAt = LocalDateTime.now();
        return offer;
    }

    public void select() {
        this.selected = true;
    }
}