package com.oow.todowithspirit.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoTokenInfoResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("app_id")
    private Integer appId;
}