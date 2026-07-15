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

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.taskType = :taskType ORDER BY t.startDate ASC")
    List<Task> findAllByUserIdAndTaskType(@Param("userId") Long userId, @Param("taskType") TaskType taskType);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.taskType = :taskType AND t.startDate BETWEEN :from AND :to ORDER BY t.startDate ASC")
    List<Task> findAllByUserIdAndTaskTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("taskType") TaskType taskType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // 캘린더: 일정은 날짜 포함, 루틴은 활성 기간 겹침 (from/to 필수)
    @Query("""
            SELECT t FROM Task t
            WHERE t.user.id = :userId
              AND (
                (t.taskType = 'SCHEDULE' AND t.startDate BETWEEN :from AND :to)
                OR (t.taskType = 'ROUTINE'
                    AND t.startDate <= :to
                    AND (t.repeatEndDate IS NULL OR t.repeatEndDate >= :from))
              )
            ORDER BY t.startDate ASC
            """)
    List<Task> findCalendarTasksWithDateRange(
            @Param("userId") Long userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.user.id = :userId")
    Optional<Task> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
