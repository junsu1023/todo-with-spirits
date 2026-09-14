package com.oow.todowithspirit.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserProfileUpdateRequest {

    @Size(min = 2, max = 12, message = "Nickname must be between 2 and 12 characters")
    private String nickname;

    private Long representativeSpiritId;
}