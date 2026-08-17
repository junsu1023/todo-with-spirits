package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritRepository;
import com.oow.todowithspirit.domain.user.OAuthProvider;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.domain.user.UserSocialAccountRepository;
import com.oow.todowithspirit.dto.user.UserProfileResponse;
import com.oow.todowithspirit.dto.user.UserProfileUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserSocialAccountRepository userSocialAccountRepository;
    private final SpiritRepository spiritRepository;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        List<OAuthProvider> providers = userSocialAccountRepository.findProvidersByUserId(userId);

        return UserProfileResponse.of(user, providers);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        Long representativeSpiritId = request.getRepresentativeSpiritId();
        if (representativeSpiritId != null) {
            validateSpiritOwnership(userId, representativeSpiritId);
        }

        user.updateProfile(request.getNickname(), request.getFullname(), request.getBirthday(),
                request.getGender(), representativeSpiritId);

        List<OAuthProvider> providers = userSocialAccountRepository.findProvidersByUserId(userId);

        return UserProfileResponse.of(user, providers);
    }

    private void validateSpiritOwnership(Long userId, Long spiritId) {
        Spirit spirit = spiritRepository.findById(spiritId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Spirit not found"));

        if (!spirit.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "You do not own this spirit");
        }
    }
}