package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpiritRepository extends JpaRepository<Spirit, Long> {

    // 특정 유저가 보유한 모든 정령 목록 조회
    List<Spirit> findAllByUserIdOrderByIdDesc(Long userId);

    // 가장 먼저 만난 정령
    Optional<Spirit> findFirstByUserIdOrderByIdAsc(Long userId);
}