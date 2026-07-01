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

    // 일정 목록 - 전체 (날짜 오름차순)
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.taskType = :taskType ORDER BY t.startDate ASC")
    List<Task> findAllByUserIdAndTaskType(@Param("userId") Long userId, @Param("taskType") TaskType taskType);

    // 일정 목록 - 날짜 범위 필터
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.taskType = :taskType AND t.startDate BETWEEN :from AND :to ORDER BY t.startDate ASC")
    List<Task> findAllByUserIdAndTaskTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("taskType") TaskType taskType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // 캘린더 통합 조회 (날짜 범위 있음)
    // - 일정: startDate가 범위 내에 포함
    // - 루틴: 활성 기간(startDate ~ repeatEndDate)이 조회 범위와 겹침
    @Query("""
            SELECT t FROM Task t
            WHERE t.user.id = :userId
              AND (
                (t.taskType = 'SCHEDULE' AND t.startDate BETWEEN :from AND :to)
                OR (t.taskType = 'HABIT'
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

    // 캘린더 통합 조회 (날짜 범위 없음) - 일정 + 루틴 전체
    @Query("""
            SELECT t FROM Task t
            WHERE t.user.id = :userId
              AND (t.taskType = 'SCHEDULE' OR t.taskType = 'HABIT')
            ORDER BY t.startDate ASC
            """)
    List<Task> findAllCalendarTasks(@Param("userId") Long userId);

    // 단건 - 소유권 + 타입 동시 검증 (타인 자원 존재 여부 노출 방지)
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.user.id = :userId AND t.taskType = :taskType")
    Optional<Task> findByIdAndUserIdAndType(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("taskType") TaskType taskType
    );
}
