package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_spirit_variant_unlocks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "variant_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSpiritVariantUnlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "unlocked_at")
    private LocalDateTime unlockedAt;

    public UserSpiritVariantUnlock(Long userId, Long variantId) {
        this.userId = userId;
        this.variantId = variantId;
        this.unlockedAt = LocalDateTime.now();
    }
}
