package com.oow.todowithspirit.dto.user;

import com.oow.todowithspirit.domain.user.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserProfileUpdateRequest {

    @Size(min = 2, max = 12, message = "Nickname must be between 2 and 12 characters")
    private String nickname;

    @Size(max = 50, message = "Fullname must be 50 characters or less")
    private String fullname;

    @Past(message = "Birthday must be in the past")
    private LocalDate birthday;

    private Gender gender;

    private Long representativeSpiritId;
}