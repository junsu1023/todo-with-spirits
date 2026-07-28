package com.oow.todowithspirit.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoutineCompletionRepository extends JpaRepository<RoutineCompletion, Long> {

    @Query("SELECT rc FROM RoutineCompletion rc WHERE rc.task.id IN :taskIds AND rc.completionDate BETWEEN :from AND :to")
    List<RoutineCompletion> findAllByTaskIdInAndCompletionDateBetween(
            @Param("taskIds") List<Long> taskIds,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("SELECT rc FROM RoutineCompletion rc JOIN rc.task t WHERE t.user.id = :userId AND rc.completionDate = :date")
    List<RoutineCompletion> findAllByUserIdAndCompletionDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    Optional<RoutineCompletion> findByTaskIdAndCompletionDate(Long taskId, LocalDate completionDate);

    List<RoutineCompletion> findAllByTaskInAndCompletionDateBetween(List<Task> routines, LocalDate startDate, LocalDate endDate);
}