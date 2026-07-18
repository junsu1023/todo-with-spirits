package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritRepository;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.spirit.RepresentativeSpiritResponse;
import com.oow.todowithspirit.dto.spirit.SpiritResponse;
import com.oow.todowithspirit.dto.spirit.UpdateRepresentativeSpiritRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
                .toList();
    }

    @Transactional(readOnly = true)
    public RepresentativeSpiritResponse getRepresentativeSpirit(Long userId) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        Long repSpiritId = user.getRepresentativeSpiritId();

        // 2. 대표 정령이 설정되어 있지 않은 경우 예외 처리
        if (repSpiritId == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Representative spirit is not set");
        }

        // 3. 대표 정령 상세 정보 조회 및 반환
        Spirit spirit = spiritRepository.findById(repSpiritId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Representative spirit not found"));

        return RepresentativeSpiritResponse.from(spirit);
    }

    @Transactional
    public void updateRepresentativeSpirit(Long userId, UpdateRepresentativeSpiritRequest request) {
        // 1. 변경하려는 정령 조회
        Spirit targetSpirit = spiritRepository.findById(request.getId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Spirit not found"));

        // 2. 소유권 검증: 이 정령이 현재 로그인한 유저의 정령이 맞는지 확인
        if (!targetSpirit.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "You do not own this spirit");
        }

        // 3. 유저의 대표 정령 ID 업데이트
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        user.setRepresentativeSpiritId(targetSpirit.getId());
    }
}
