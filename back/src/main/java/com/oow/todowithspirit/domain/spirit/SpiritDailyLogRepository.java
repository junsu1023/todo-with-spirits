package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpiritDailyLogRepository extends JpaRepository<SpiritDailyLog, Long> {

    boolean existsBySpiritIdAndLogDate(Long spiritId, LocalDate logDate);

    List<SpiritDailyLog> findAllBySpiritIdOrderByLogDateDesc(Long spiritId);
}