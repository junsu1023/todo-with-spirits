package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpiritPlaceActionRepository extends JpaRepository<SpiritPlaceAction, Long> {

    List<SpiritPlaceAction> findAllByPlaceIdOrderBySortOrderAsc(Long placeId);

    List<SpiritPlaceAction> findAllByOrderBySortOrderAsc();
}