package com.oow.todowithspirit.dto.spirit;

import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritStatus;
import com.oow.todowithspirit.domain.spirit.SpiritTemperament;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class SpiritHistoryResponse {

    private final Long id;
    private final String spiritName;
    private final SpiritStatus status;
    private final List<SpiritTemperament> temperaments;
    private final String careerGrowthType;
    private final String imageUrl;
    private final LocalDateTime startedAt;
    private final LocalDateTime maturedAt;
    private final LocalDateTime departedAt;

    public static SpiritHistoryResponse of(Spirit spirit, List<SpiritTemperament> temperaments) {
        return new SpiritHistoryResponse(
                spirit.getId(),
                spirit.getSpiritName(),
                spirit.getStatus(),
                temperaments,
                spirit.getCareerGrowthType() != null ? spirit.getCareerGrowthType().name() : null,
                spirit.getImageUrl(),
                spirit.getCreatedAt(),
                spirit.getMaturedAt(),
                spirit.getDepartedAt()
        );
    }
}