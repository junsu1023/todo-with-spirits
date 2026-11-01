package com.oow.todowithspirit.dto.spirit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateSpiritNameRequest {

    @NotBlank(message = "Spirit name is required")
    @Size(max = 50, message = "Spirit name must be 50 characters or less")
    private String spiritName;
}
