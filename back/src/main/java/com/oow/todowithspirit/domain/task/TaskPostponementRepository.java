package com.oow.todowithspirit.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskPostponementRepository extends JpaRepository<TaskPostponement, Long> {

    long countByTaskIdAndOriginalDate(Long taskId, LocalDate originalDate);

    List<TaskPostponement> findAllByTaskId(Long taskId);

    Optional<TaskPostponement> findTopByTaskIdAndOriginalDateOrderByCreatedAtDesc(Long taskId, LocalDate originalDate);

    Optional<TaskPostponement> findTopByTaskIdOrderByCreatedAtAsc(Long taskId);

    // 카테고리별 미룸 횟수 집계 (일간/주간/월간 리포트용)
    @Query("""
            SELECT p.category AS category, COUNT(p) AS count
            FROM TaskPostponement p
            WHERE p.userId = :userId AND p.originalDate BETWEEN :from AND :to
            GROUP BY p.category
            """)
    List<CategoryPostponeCount> countByCategory(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    interface CategoryPostponeCount {
        CategoryType getCategory();

        long getCount();
    }
}