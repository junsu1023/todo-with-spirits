package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSpiritHomeObjectRepository extends JpaRepository<UserSpiritHomeObject, Long> {

    List<UserSpiritHomeObject> findAllByUserId(Long userId);

    Optional<UserSpiritHomeObject> findByUserIdAndObjectId(Long userId, Long objectId);

    boolean existsByUserIdAndObjectId(Long userId, Long objectId);
}