package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.SpiritDiscovery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SpiritDiscoveryResponse {

    private final Long id;
    private final Long spiritId;
    private final String itemName;
    private final String description;
    private final String iconUrl;
    private final LocalDateTime discoveredAt;

    public static SpiritDiscoveryResponse from(SpiritDiscovery discovery) {
        return new SpiritDiscoveryResponse(
                discovery.getId(),
                discovery.getSpiritId(),
                discovery.getItemName(),
                discovery.getDescription(),
                discovery.getIconUrl(),
                discovery.getDiscoveredAt()
        );
    }
}