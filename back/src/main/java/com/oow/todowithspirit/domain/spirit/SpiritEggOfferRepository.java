package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpiritEggOfferRepository extends JpaRepository<SpiritEggOffer, Long> {

    List<SpiritEggOffer> findAllByUserIdAndSelectedFalseOrderByIdAsc(Long userId);

    void deleteAllByUserIdAndSelectedFalse(Long userId);
}