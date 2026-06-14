package com.oow.todowithspirit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TodoWithSpiritApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoWithSpiritApplication.class, args);
    }
}