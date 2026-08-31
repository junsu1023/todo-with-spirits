package com.oow.todowithspirit.dto.spirit;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateRepresentativeSpiritRequest {

    @NotNull(message = "Spirit ID to change is required")
    private Long id;
}