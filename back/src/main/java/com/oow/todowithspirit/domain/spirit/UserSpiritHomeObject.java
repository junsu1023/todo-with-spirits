package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_spirit_home_objects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserSpiritHomeObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Column(name = "installed_at")
    private LocalDateTime installedAt;

    public UserSpiritHomeObject(Long userId, Long objectId) {
        this.userId = userId;
        this.objectId = objectId;
        this.installedAt = LocalDateTime.now();
    }
}