package ru.nesterov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@SpringBootApplication
public class ClientRevenueCalendarApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientRevenueCalendarApplication.class, args);
    }
}
