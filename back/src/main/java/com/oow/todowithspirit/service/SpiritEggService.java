package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.*;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.spirit.EggOfferResponse;
import com.oow.todowithspirit.dto.spirit.RepresentativeSpiritResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 초기 튜토리얼 알(SpiritService.createDefaultSpirit)을 제외한, 이후 정령의 알 선택(3~5개 중 1개)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpiritEggService {

    private static final int MIN_OFFER_COUNT = 3;
    private static final int MAX_OFFER_COUNT = 5;

    private final SpiritEggOptionRepository spiritEggOptionRepository;
    private final SpiritEggOfferRepository spiritEggOfferRepository;
    private final SpiritService spiritService;
    private final UserRepository userRepository;

    @Transactional
    public List<EggOfferResponse> getOrCreateOffers(Long userId) {
        List<SpiritEggOffer> offers = spiritEggOfferRepository.findAllByUserIdAndSelectedFalseOrderByIdAsc(userId);
        if (offers.isEmpty()) {
            offers = createOffers(userId);
        }
        return toResponses(offers);
    }

    @Transactional
    public RepresentativeSpiritResponse selectEgg(Long userId, Long offerId) {
        SpiritEggOffer offer = spiritEggOfferRepository.findById(offerId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Egg offer not found"));

        if (!offer.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "You do not own this egg offer");
        }
        if (offer.isSelected()) {
            throw new ApiException(ErrorCode.ALREADY_COMPLETED, "Egg offer is already selected");
        }

        SpiritEggOption egg = spiritEggOptionRepository.findById(offer.getEggOptionId())
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Egg option not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        Spirit spirit = spiritService.createFromEggOption(user, egg);

        offer.select();
        spiritEggOfferRepository.deleteAllByUserIdAndSelectedFalse(userId);

        return spiritService.toResponse(spirit);
    }

    private List<SpiritEggOffer> createOffers(Long userId) {
        List<SpiritEggOption> pool = new ArrayList<>(spiritEggOptionRepository.findAllByIsActiveTrue().stream()
                .filter(egg -> !egg.isTutorial())
                .toList());
        Collections.shuffle(pool);

        int offerCount = Math.min(pool.size(), ThreadLocalRandom.current().nextInt(MIN_OFFER_COUNT, MAX_OFFER_COUNT + 1));
        List<SpiritEggOffer> offers = pool.stream()
                .limit(offerCount)
                .map(egg -> SpiritEggOffer.of(userId, egg.getId()))
                .toList();

        return spiritEggOfferRepository.saveAll(offers);
    }

    private List<EggOfferResponse> toResponses(List<SpiritEggOffer> offers) {
        return offers.stream()
                .map(offer -> {
                    SpiritEggOption egg = spiritEggOptionRepository.findById(offer.getEggOptionId())
                            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Egg option not found"));
                    return EggOfferResponse.of(offer.getId(), egg);
                })
                .toList();
    }
}