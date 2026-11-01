package com.oow.todowithspirit.domain.spirit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spirit_place_actions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpiritPlaceAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "action_text", nullable = false, length = 50)
    private String actionText;

    @Column(name = "requires_object", nullable = false)
    private boolean requiresObject;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}