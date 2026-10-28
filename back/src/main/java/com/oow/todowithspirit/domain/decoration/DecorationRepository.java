package com.oow.todowithspirit.domain.decoration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecorationRepository extends JpaRepository<Decoration, Long> {

    List<Decoration> findAllByOrderByIdAsc();
}
