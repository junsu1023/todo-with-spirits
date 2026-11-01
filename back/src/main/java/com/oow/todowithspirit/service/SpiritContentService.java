package com.oow.todowithspirit.service;

import com.oow.todowithspirit.common.exception.ApiException;
import com.oow.todowithspirit.common.exception.ErrorCode;
import com.oow.todowithspirit.domain.spirit.*;
import com.oow.todowithspirit.domain.task.CategoryType;
import com.oow.todowithspirit.dto.spirit.ActionPlaceResponse;
import com.oow.todowithspirit.dto.spirit.CategoryActionResponse;
import com.oow.todowithspirit.dto.spirit.HomeObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 정령의 행동장소/카테고리 표현/집 오브젝트 등 마스터 콘텐츠 조회 및 오브젝트 설치 관리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpiritContentService {

    private final SpiritActionPlaceMasterRepository spiritActionPlaceMasterRepository;
    private final SpiritPlaceActionRepository spiritPlaceActionRepository;
    private final CategorySpiritActionRepository categorySpiritActionRepository;
    private final SpiritHomeObjectRepository spiritHomeObjectRepository;
    private final UserSpiritHomeObjectRepository userSpiritHomeObjectRepository;

    public List<ActionPlaceResponse> getActionPlaces() {
        return spiritActionPlaceMasterRepository.findAllByOrderBySortOrderAsc().stream()
                .map(place -> ActionPlaceResponse.of(place, spiritPlaceActionRepository.findAllByPlaceIdOrderBySortOrderAsc(place.getId())))
                .toList();
    }

    public List<CategoryActionResponse> getCategoryActions(CategoryType category) {
        List<CategorySpiritAction> actions = category != null
                ? categorySpiritActionRepository.findAllByCategoryOrderBySortOrderAsc(category)
                : categorySpiritActionRepository.findAllByOrderByCategoryAscSortOrderAsc();

        return actions.stream().map(CategoryActionResponse::from).toList();
    }

    public List<HomeObjectResponse> getHomeObjects(Long userId) {
        List<UserSpiritHomeObject> installed = userSpiritHomeObjectRepository.findAllByUserId(userId);
        List<Long> installedObjectIds = installed.stream().map(UserSpiritHomeObject::getObjectId).toList();

        return spiritHomeObjectRepository.findAllByOrderByPlaceIdAsc().stream()
                .map(object -> HomeObjectResponse.of(object, installedObjectIds.contains(object.getId())))
                .toList();
    }

    @Transactional
    public void installHomeObject(Long userId, Long objectId) {
        if (!spiritHomeObjectRepository.existsById(objectId)) {
            throw new ApiException(ErrorCode.NOT_FOUND, "Home object not found");
        }
        if (userSpiritHomeObjectRepository.existsByUserIdAndObjectId(userId, objectId)) {
            throw new ApiException(ErrorCode.ALREADY_COMPLETED, "Already installed");
        }
        userSpiritHomeObjectRepository.save(new UserSpiritHomeObject(userId, objectId));
    }

    @Transactional
    public void uninstallHomeObject(Long userId, Long objectId) {
        UserSpiritHomeObject installed = userSpiritHomeObjectRepository.findByUserIdAndObjectId(userId, objectId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "Object is not installed"));
        userSpiritHomeObjectRepository.delete(installed);
    }
}