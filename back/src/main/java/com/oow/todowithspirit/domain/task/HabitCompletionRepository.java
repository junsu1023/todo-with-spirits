package com.oow.todowithspirit.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitCompletionRepository extends JpaRepository<HabitCompletion, Long> {

    @Query("SELECT hc FROM HabitCompletion hc WHERE hc.task.id IN :taskIds AND hc.completionDate BETWEEN :from AND :to")
    List<HabitCompletion> findAllByTaskIdInAndCompletionDateBetween(
            @Param("taskIds") List<Long> taskIds,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    Optional<HabitCompletion> findByTaskIdAndCompletionDate(Long taskId, LocalDate completionDate);
}