package com.oow.todowithspirit.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TaskAffectionGrantRepository extends JpaRepository<TaskAffectionGrant, Long> {

    boolean existsByTaskIdAndCompletionDate(Long taskId, LocalDate completionDate);

    boolean existsByTaskId(Long taskId);
}