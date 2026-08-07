package com.oow.todowithspirit.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SocialLoginRequest {

    @NotBlank(message = "Provider is required")
    private String provider;

    @NotBlank(message = "Provider User Id is required")
    private String providerUserId;

    private String email;
}