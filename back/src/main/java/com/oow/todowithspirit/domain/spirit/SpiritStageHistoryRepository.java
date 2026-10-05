package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpiritStageHistoryRepository extends JpaRepository<SpiritStageHistory, Long> {

}