package com.oow.todowithspirit.domain.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday, Long> {

    // 특정 날짜가 공휴일 테이블에 존재하는지 확인
    boolean existsByHolidayDate(LocalDate date);

    // 시작일과 종료일 사이(Between)에 존재하는 모든 공휴일 목록 조회 (성능 최적화용)
    List<Holiday> findAllByHolidayDateBetween(LocalDate start, LocalDate end);
}