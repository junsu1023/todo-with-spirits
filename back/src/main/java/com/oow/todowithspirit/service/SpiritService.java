package com.oow.todowithspirit.service;

import com.oow.todowithspirit.domain.spirit.SpiritRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SpiritService {

    private final SpiritRepository spiritRepository;
}
