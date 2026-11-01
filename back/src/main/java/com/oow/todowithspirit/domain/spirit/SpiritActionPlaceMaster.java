package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spirit_action_places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritActionPlaceMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private SpiritActionPlace code;

    @Column(name = "place_name", nullable = false, length = 50)
    private String placeName;

    @Column(name = "representative_action", nullable = false, length = 50)
    private String representativeAction;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
