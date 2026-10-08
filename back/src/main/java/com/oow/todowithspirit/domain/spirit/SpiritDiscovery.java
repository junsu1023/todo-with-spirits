package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "spirit_discoveries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritDiscovery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spirit_id", nullable = false)
    private Long spiritId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "discovered_at")
    private LocalDateTime discoveredAt;
}