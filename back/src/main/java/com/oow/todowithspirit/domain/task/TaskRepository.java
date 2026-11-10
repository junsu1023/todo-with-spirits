package com.oow.todowithspirit.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // 기간 조회: 단발성은 날짜 포함, 반복은 활성 기간 겹침
    @Query("""
            SELECT t FROM Task t
            WHERE t.user.id = :userId
              AND (
                (t.repeatType = com.oow.todowithspirit.domain.task.RepeatType.NONE
                    AND t.startDate BETWEEN :from AND :to)
                OR (t.repeatType <> com.oow.todowithspirit.domain.task.RepeatType.NONE
                    AND t.startDate <= :to
                    AND (t.repeatEndDate IS NULL OR t.repeatEndDate >= :from))
              )
            ORDER BY t.startDate ASC
            """)
    List<Task> findAllInDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.user.id = :userId")
    Optional<Task> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<Task> findByUserIdAndEndDate(Long userId, LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.startDate <= :endDate AND t.endDate >= :startDate")
    List<Task> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
}
