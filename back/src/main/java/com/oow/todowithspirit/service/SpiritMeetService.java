package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.currency.CurrencyReason;
import com.oow.todowithspirit.domain.spirit.Spirit;
import com.oow.todowithspirit.domain.spirit.SpiritRepository;
import com.oow.todowithspirit.domain.spirit.SpiritSpecies;
import com.oow.todowithspirit.domain.spirit.SpiritSpeciesRepository;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.spirit.SpiritMeetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * "정령 만나기" 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpiritMeetService {

    // todo: 기획 확정 전 임시 가격
    private static final long MEET_COST = 100;

    private final SpiritRepository spiritRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;
    private final SpiritSpeciesRepository spiritSpeciesRepository;
    private final SpiritService spiritService;

    /**
     * 재화를 소비해 아직 보유하지 않은 정령 종류 중 하나를 무작위로 만난다 (종류당 1마리).
     * 기존 정령은 사라지지 않으며, 새로 만난 정령이 대표(동행)로 지정된다.
     */
    @Transactional
    public SpiritMeetResponse meet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        Set<Long> ownedSpeciesIds = spiritService.ownedSpiritsBySpecies(userId, user.getRepresentativeSpiritId()).keySet();
        List<SpiritSpecies> unmetSpecies = spiritSpeciesRepository.findAllByOrderBySortOrderAsc().stream()
                .filter(species -> !ownedSpeciesIds.contains(species.getId()))
                .toList();
        if (unmetSpecies.isEmpty()) {
            throw new ApiException(ErrorCode.ALL_SPECIES_MET, "You have already met all the spirits");
        }

        currencyService.debit(userId, MEET_COST, CurrencyReason.SPIRIT_MEET);

        SpiritSpecies picked = unmetSpecies.get(ThreadLocalRandom.current().nextInt(unmetSpecies.size()));
        Spirit spirit = new Spirit(user, picked);
        spiritRepository.save(spirit);
        user.setRepresentativeSpiritId(spirit.getId());

        return new SpiritMeetResponse(spirit.getId(), spirit.getSpiritName(), currencyService.getBalance(userId));
    }
}
