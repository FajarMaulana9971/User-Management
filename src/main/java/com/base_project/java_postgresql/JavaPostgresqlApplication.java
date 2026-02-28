package com.base_project.java_postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
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
