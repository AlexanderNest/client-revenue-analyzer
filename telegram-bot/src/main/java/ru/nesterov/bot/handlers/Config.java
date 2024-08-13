package ru.nesterov.bot.handlers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class Config {
    @Bean
    public List<CommandHandler> commandHandlers(GetMonthStatisticsHandler getMonthStatisticsHandler) {
        return new ArrayList<>() {{
           add(getMonthStatisticsHandler);
        }};
    }
}
