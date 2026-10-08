package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpiritLetterRepository extends JpaRepository<SpiritLetter, Long> {

    List<SpiritLetter> findAllByUserIdOrderBySentAtDesc(Long userId);

    List<SpiritLetter> findAllBySpiritIdOrderBySentAtDesc(Long spiritId);
}