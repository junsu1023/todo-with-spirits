package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpiritEggOptionRepository extends JpaRepository<SpiritEggOption, Long> {

    List<SpiritEggOption> findAllByIsActiveTrue();

    Optional<SpiritEggOption> findFirstByIsTutorialTrueAndIsActiveTrue();
}