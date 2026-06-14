package com.oow.todowithspirit.controller;

import com.oow.todowithspirit.service.SpiritService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spirit")
@RequiredArgsConstructor
public class SpiritController {

    private final SpiritService spiritService;
}
