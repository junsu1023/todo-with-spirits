package com.oow.todowithspirit.domain.decoration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_decorations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "decoration_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserDecoration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "decoration_id", nullable = false)
    private Long decorationId;

    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

    @Column(name = "is_installed", nullable = false)
    private boolean installed;

    public UserDecoration(Long userId, Long decorationId) {
        this.userId = userId;
        this.decorationId = decorationId;
        this.purchasedAt = LocalDateTime.now();
        this.installed = false;
    }

    public void install() {
        this.installed = true;
    }

    public void uninstall() {
        this.installed = false;
    }
}
