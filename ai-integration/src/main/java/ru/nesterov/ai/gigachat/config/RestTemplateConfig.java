package ru.nesterov.ai.gigachat.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate gigachatRestTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(NonValidatingClientHttpRequestFactory::new)
                .build();
    }
}