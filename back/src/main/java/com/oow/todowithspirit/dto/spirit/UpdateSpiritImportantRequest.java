package com.oow.todowithspirit.dto.spirit;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateSpiritImportantRequest {

    @NotNull(message = "isImportant is required")
    private Boolean isImportant;
}
