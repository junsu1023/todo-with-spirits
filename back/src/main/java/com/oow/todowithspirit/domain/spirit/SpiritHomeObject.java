package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spirit_home_objects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritHomeObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "object_name", nullable = false, length = 50)
    private String objectName;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "icon_url")
    private String iconUrl;
}