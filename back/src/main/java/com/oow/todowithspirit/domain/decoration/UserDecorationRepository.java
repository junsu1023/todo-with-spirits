package com.oow.todowithspirit.domain.decoration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDecorationRepository extends JpaRepository<UserDecoration, Long> {

    List<UserDecoration> findAllByUserId(Long userId);

    Optional<UserDecoration> findByUserIdAndDecorationId(Long userId, Long decorationId);

    boolean existsByUserIdAndDecorationId(Long userId, Long decorationId);
}
