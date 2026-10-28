package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSpiritVariantUnlockRepository extends JpaRepository<UserSpiritVariantUnlock, Long> {

    List<UserSpiritVariantUnlock> findAllByUserId(Long userId);
}
