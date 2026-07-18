package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritRepository;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.spirit.SpiritResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SpiritService {

    private final SpiritRepository spiritRepository;
    private final UserRepository userRepository; // 유저의 대표 정령 ID를 가져오기 위함

    /**
     * 사용자가 보유한 정령 목록 조회
     * 대표 정령(isEquipped=true) 최우선 조회(朝會)
     */
    @Transactional(readOnly = true)
    public List<SpiritResponse> getUserSpirits(Long userId) {
        // 1. 유저 정보 조회하여 대표 정령 ID 확보
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
        Long repSpiritId = user.getRepresentativeSpiritId();

        // 2. 유저가 보유한 전체 정령 리스트 조회
        List<Spirit> spirits = spiritRepository.findAllByUserIdOrderByIdDesc(userId);

        // 3. 대표 정령 여부를 판별하며 DTO 맵핑
        return spirits.stream()
                .map(spirit -> SpiritResponse.of(spirit, repSpiritId))
                .collect(Collectors.toList());
    }
}
