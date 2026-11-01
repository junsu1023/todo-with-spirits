package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.currency.CurrencyReason;
import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritRepository;
import com.oow.todowithspirit.domain.spirit.SpiritVariant;
import com.oow.todowithspirit.domain.spirit.SpiritVariantRepository;
import com.oow.todowithspirit.domain.spirit.UserSpiritVariantUnlock;
import com.oow.todowithspirit.domain.spirit.UserSpiritVariantUnlockRepository;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.spirit.SpiritMeetResponse;
import com.oow.todowithspirit.dto.spirit.SpiritVariantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 정령 외형(변형) 해금 현황 조회 및 "정령 만나기"(뽑기) 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpiritMeetService {

    // todo: 기획 확정 전 임시 가격
    private static final long MEET_COST = 100;

    private final SpiritVariantRepository spiritVariantRepository;
    private final UserSpiritVariantUnlockRepository userSpiritVariantUnlockRepository;
    private final SpiritRepository spiritRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;

    public List<SpiritVariantResponse> getVariants(Long userId) {
        Set<Long> unlockedIds = unlockedVariantIds(userId);

        return spiritVariantRepository.findAllByOrderBySortOrderAsc().stream()
                .map(variant -> SpiritVariantResponse.of(variant, variant.isAlwaysUnlocked() || unlockedIds.contains(variant.getId())))
                .toList();
    }

    /**
     * 재화를 소비해 그 시점에 해금된 외형 중 하나를 무작위로 뽑아 새 정령을 만난다.
     * 기존 정령은 사라지지 않으며, 새로 만난 정령이 대표(동행)로 지정된다.
     */
    @Transactional
    public SpiritMeetResponse meet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        List<SpiritVariant> pool = unlockedVariants(userId);
        if (pool.isEmpty()) {
            throw new ApiException(ErrorCode.NO_UNLOCKED_VARIANT, "해금된 정령 외형이 없습니다");
        }

        currencyService.debit(userId, MEET_COST, CurrencyReason.SPIRIT_MEET);

        SpiritVariant picked = drawByWeight(pool);
        Spirit spirit = new Spirit(user, picked);
        spiritRepository.save(spirit);
        user.setRepresentativeSpiritId(spirit.getId());

        return new SpiritMeetResponse(spirit.getId(), spirit.getSpiritName(), picked.getCode(), currencyService.getBalance(userId));
    }

    private List<SpiritVariant> unlockedVariants(Long userId) {
        Set<Long> unlockedIds = unlockedVariantIds(userId);
        return spiritVariantRepository.findAll().stream()
                .filter(variant -> variant.isAlwaysUnlocked() || unlockedIds.contains(variant.getId()))
                .toList();
    }

    private Set<Long> unlockedVariantIds(Long userId) {
        return userSpiritVariantUnlockRepository.findAllByUserId(userId).stream()
                .map(UserSpiritVariantUnlock::getVariantId)
                .collect(Collectors.toSet());
    }

    private SpiritVariant drawByWeight(List<SpiritVariant> pool) {
        int totalWeight = pool.stream().mapToInt(SpiritVariant::getWeight).sum();
        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        int cursor = 0;
        for (SpiritVariant variant : pool) {
            cursor += variant.getWeight();
            if (roll < cursor) {
                return variant;
            }
        }
        return pool.get(pool.size() - 1); // 정수 가중치 합산이라 이론상 도달하지 않음
    }
}
