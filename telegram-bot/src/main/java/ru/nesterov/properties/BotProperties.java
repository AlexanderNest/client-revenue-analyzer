package ru.nesterov.properties;

import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("bot")
@Configuration
@Data
public class BotProperties {
    private String apiToken;
    private String username;
    private String secretToken;
}
