package ru.nesterov.gigachat;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "giga-chat")
@Data
public class GigaChatIntegrationProperties {
    private String authKey;
    private String authUrl;
    private String baseUrl;
    private String textGenerationUrl;
}
