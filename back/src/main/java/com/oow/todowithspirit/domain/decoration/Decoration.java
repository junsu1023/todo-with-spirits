package com.oow.todowithspirit.domain.decoration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "decorations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Decoration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "decoration_name", nullable = false, length = 50)
    private String decorationName;

    @Column(nullable = false)
    private long price;

    @Column(name = "asset_key", nullable = false, length = 100)
    private String assetKey;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_file_name", length = 150)
    private String imageFileName;
}
