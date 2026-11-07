package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.*;
import com.oow.todowithspirit.domain.user.User;
import com.oow.todowithspirit.domain.user.UserRepository;
import com.oow.todowithspirit.dto.spirit.RepresentativeSpiritResponse;
import com.oow.todowithspirit.dto.spirit.SpiritCollectionResponse;
import com.oow.todowithspirit.dto.spirit.UpdateRepresentativeSpiritRequest;
import com.oow.todowithspirit.dto.spirit.UpdateSpiritNameRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SpiritService {

    private static final String DEFAULT_IMAGE_URL = "https://default_baby_spirit.png"; // todo: 기본 에셋 위치 변경
    private static final int MIN_TEMPERAMENTS = 2;
    private static final int MAX_TEMPERAMENTS = 3;

    private final SpiritRepository spiritRepository;
    private final UserRepository userRepository; // 유저의 대표 정령 ID를 가져오기 위함
    private final SpiritEggOptionRepository spiritEggOptionRepository;
    private final SpiritTemperamentTagRepository spiritTemperamentTagRepository;
    private final SpiritSpeciesRepository spiritSpeciesRepository;
    private final SpiritVariantRepository spiritVariantRepository;

    /**
     * 정령도감 조회.
     * 전체 정령 종류를 정렬 순서대로 반환하고, 사용자가 보유한 종류는 그 정령의 호감도·중요·완료 여부를 함께 담는다.
     */
    @Transactional(readOnly = true)
    public List<SpiritCollectionResponse> getSpiritCollection(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));
        Long repSpiritId = user.getRepresentativeSpiritId();

        Map<Long, Spirit> ownedBySpecies = ownedSpiritsBySpecies(userId, repSpiritId);

        return spiritSpeciesRepository.findAllByOrderBySortOrderAsc().stream()
                .map(species -> {
                    Spirit spirit = ownedBySpecies.get(species.getId());
                    return spirit == null
                            ? SpiritCollectionResponse.notOwned(species)
                            : SpiritCollectionResponse.owned(species, spirit, repSpiritId);
                })
                .toList();
    }

    /**
     * 사용자가 보유한 정령을 종류별로 묶는다 (종류당 1마리).
     * 종류가 지정되지 않은 예전 정령은 외형의 종류 → 기본 종류 순으로 귀속시킨다.
     * 예전 데이터로 같은 종류의 정령이 여러 마리면 대표 정령 → 가장 먼저 만난 정령을 사용한다.
     */
    public Map<Long, Spirit> ownedSpiritsBySpecies(Long userId, Long representativeSpiritId) {
        List<Spirit> spirits = spiritRepository.findAllByUserIdOrderByIdDesc(userId);
        if (spirits.isEmpty()) {
            return Map.of();
        }

        Map<Long, Long> speciesByVariant = spiritVariantRepository.findAll().stream()
                .filter(variant -> variant.getSpeciesId() != null)
                .collect(Collectors.toMap(SpiritVariant::getId, SpiritVariant::getSpeciesId));
        Long defaultSpeciesId = spiritSpeciesRepository.findFirstByOrderBySortOrderAscIdAsc()
                .map(SpiritSpecies::getId)
                .orElse(null);

        Map<Long, Spirit> result = new HashMap<>();
        // id 내림차순이므로 뒤에 오는(먼저 만난) 정령이 덮어쓰고, 대표 정령은 마지막에 우선 적용
        for (Spirit spirit : spirits) {
            Long speciesId = resolveSpeciesId(spirit, speciesByVariant, defaultSpeciesId);
            if (speciesId != null) {
                result.put(speciesId, spirit);
            }
        }
        spirits.stream()
                .filter(spirit -> spirit.getId().equals(representativeSpiritId))
                .findFirst()
                .ifPresent(rep -> {
                    Long speciesId = resolveSpeciesId(rep, speciesByVariant, defaultSpeciesId);
                    if (speciesId != null) {
                        result.put(speciesId, rep);
                    }
                });
        return result;
    }

    private Long resolveSpeciesId(Spirit spirit, Map<Long, Long> speciesByVariant, Long defaultSpeciesId) {
        if (spirit.getSpeciesId() != null) {
            return spirit.getSpeciesId();
        }
        if (spirit.getVariantId() != null && speciesByVariant.containsKey(spirit.getVariantId())) {
            return speciesByVariant.get(spirit.getVariantId());
        }
        return defaultSpeciesId;
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

        return RepresentativeSpiritResponse.from(spirit, temperamentsOf(spirit.getId()));
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

    @Transactional
    public void updateSpiritName(Long userId, Long spiritId, UpdateSpiritNameRequest request) {
        Spirit spirit = spiritRepository.findById(spiritId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Spirit not found"));

        if (!spirit.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "You do not own this spirit");
        }

        spirit.updateName(request.getSpiritName());
    }

    /* 대표 정령 조회 및 없으면 기본 정령 자동 생성 */
    @Transactional
    public Spirit getOrCreateRepresentativeSpirit(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "User not found"));

        if (user.getRepresentativeSpiritId() != null) {
            return spiritRepository.findById(user.getRepresentativeSpiritId())
                    .orElseGet(() -> createDefaultSpirit(user)); // ID는 있으나 데이터가 지워진 경우 대비
        } else {
            return createDefaultSpirit(user); // 과거 계정용 자동 생성
        }
    }

    /**
     * 기본 정령 생성 헬퍼 로직.
     * 최초 가입 시 알 선택 없이 튜토리얼 알(is_tutorial=true)로 자동 배정한다.
     */
    @Transactional
    public Spirit createDefaultSpirit(User user) {
        Spirit defaultSpirit = spiritEggOptionRepository.findFirstByIsTutorialTrueAndIsActiveTrue()
                .map(egg -> new Spirit(user, StringUtils.hasText(egg.getImageUrl()) ? egg.getImageUrl() : DEFAULT_IMAGE_URL, egg.getId()))
                .orElseGet(() -> new Spirit(user, DEFAULT_IMAGE_URL));

        // 첫 정령은 기본 종류의 기본 외형으로 지정
        spiritSpeciesRepository.findFirstByOrderBySortOrderAscIdAsc().ifPresent(species -> {
            Long defaultVariantId = spiritVariantRepository.findAllBySpeciesIdOrderBySortOrderAsc(species.getId()).stream()
                    .filter(SpiritVariant::isAlwaysUnlocked)
                    .map(SpiritVariant::getId)
                    .findFirst()
                    .orElse(null);
            defaultSpirit.assignSpecies(species.getId(), defaultVariantId);
        });

        spiritRepository.save(defaultSpirit);
        assignRandomTemperaments(defaultSpirit.getId());
        user.setRepresentativeSpiritId(defaultSpirit.getId());

        return defaultSpirit;
    }

    private void assignRandomTemperaments(Long spiritId) {
        List<SpiritTemperament> pool = new ArrayList<>(List.of(SpiritTemperament.values()));
        Collections.shuffle(pool);
        int count = ThreadLocalRandom.current().nextInt(MIN_TEMPERAMENTS, MAX_TEMPERAMENTS + 1);
        pool.stream()
                .limit(count)
                .forEach(temperament -> spiritTemperamentTagRepository.save(new SpiritTemperamentTag(spiritId, temperament)));
    }

    private List<SpiritTemperament> temperamentsOf(Long spiritId) {
        return spiritTemperamentTagRepository.findAllBySpiritId(spiritId).stream()
                .map(SpiritTemperamentTag::getTemperament)
                .toList();
    }
}