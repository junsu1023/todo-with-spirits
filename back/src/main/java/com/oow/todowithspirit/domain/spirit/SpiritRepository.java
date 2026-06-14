package com.oow.todowithspirit.domain.spirit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpiritRepository extends JpaRepository<Spirit, Long> {

}
