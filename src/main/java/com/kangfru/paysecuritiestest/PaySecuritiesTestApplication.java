package com.kangfru.paysecuritiestest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaySecuritiesTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaySecuritiesTestApplication.class, args);
    }

}
