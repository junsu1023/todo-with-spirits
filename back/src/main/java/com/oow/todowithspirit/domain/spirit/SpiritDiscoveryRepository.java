package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpiritDiscoveryRepository extends JpaRepository<SpiritDiscovery, Long> {

    List<SpiritDiscovery> findAllByUserIdOrderByDiscoveredAtDesc(Long userId);

    List<SpiritDiscovery> findAllBySpiritIdOrderByDiscoveredAtDesc(Long spiritId);
}