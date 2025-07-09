package ru.nesterov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@EnableCaching
@EnableScheduling
@SpringBootApplication
public class ClientRevenueCalendarApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientRevenueCalendarApplication.class, args);
    }
}
