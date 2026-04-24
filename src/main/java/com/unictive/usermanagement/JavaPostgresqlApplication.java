package com.unictive.usermanagement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableAsync
@Slf4j
public class JavaPostgresqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaPostgresqlApplication.class, args);
        log.info("");
        log.info("=================================");
        log.info("Application Successfully Started");
        log.info("=================================");
        log.info("");
    }

}
