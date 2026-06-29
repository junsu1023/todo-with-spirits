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
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.type = :type ORDER BY t.taskDate ASC")
    List<Task> findAllByUserIdAndType(@Param("userId") Long userId, @Param("type") TaskType type);

    // 일정 목록 - 날짜 범위 필터
    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.type = :type AND t.taskDate BETWEEN :from AND :to ORDER BY t.taskDate ASC")
    List<Task> findAllByUserIdAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") TaskType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    // 단건 - 소유권 + 타입 동시 검증 (타인 자원 존재 여부 노출 방지)
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.user.id = :userId AND t.type = :type")
    Optional<Task> findByIdAndUserIdAndType(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("type") TaskType type
    );
}
