package com.oow.todowithspirit.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskPostponementRepository extends JpaRepository<TaskPostponement, Long> {

    long countByTaskIdAndOriginalDate(Long taskId, LocalDate originalDate);

    List<TaskPostponement> findAllByTaskId(Long taskId);

    Optional<TaskPostponement> findTopByTaskIdAndOriginalDateOrderByCreatedAtDesc(Long taskId, LocalDate originalDate);

    Optional<TaskPostponement> findTopByTaskIdOrderByCreatedAtAsc(Long taskId);
}