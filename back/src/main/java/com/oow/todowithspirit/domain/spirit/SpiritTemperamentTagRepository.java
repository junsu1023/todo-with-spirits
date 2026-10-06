package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpiritTemperamentTagRepository extends JpaRepository<SpiritTemperamentTag, Long> {

    List<SpiritTemperamentTag> findAllBySpiritId(Long spiritId);
}