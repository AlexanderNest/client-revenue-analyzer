package ru.nesterov.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
@Slf4j
@ConditionalOnProperty("bot.enabled")
public class BotConfig {
    @Bean
    public TelegramBotsApi telegramBotsApi(RevenueAnalyzerBot revenueAnalyzerBot) throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class) {{
            registerBot(revenueAnalyzerBot);
        }};
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
