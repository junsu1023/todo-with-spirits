package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpiritSpeciesRepository extends JpaRepository<SpiritSpecies, Long> {

    List<SpiritSpecies> findAllByOrderBySortOrderAsc();

    // 종류가 지정되지 않은 예전 정령(알로 생성된 정령 등)을 귀속시킬 기본 종류
    Optional<SpiritSpecies> findFirstByOrderBySortOrderAscIdAsc();
}
