package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpiritHomeObjectRepository extends JpaRepository<SpiritHomeObject, Long> {

    List<SpiritHomeObject> findAllByOrderByPlaceIdAsc();
}