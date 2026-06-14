package ru.nesterov.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.HeaderFilters;
import org.zalando.logbook.core.QueryFilters;
import org.zalando.logbook.json.JsonBodyFilters;

import java.util.function.Predicate;

import static org.zalando.logbook.core.HeaderFilters.defaultValue;

@Configuration
public class LogbookSecurityConfig {

    private static final String REPLACEMENT = "<masked>";

    @Bean
    public Logbook logbook() {
        return Logbook.builder()
                .headerFilter(defaultValue())
                .headerFilter(HeaderFilters.replaceHeaders(paramPredicate, REPLACEMENT))
                .queryFilter(QueryFilters.replaceQuery(paramPredicate, REPLACEMENT))
                .bodyFilter(JsonBodyFilters.replaceJsonStringProperty(paramPredicate, REPLACEMENT))
                .build();
    }

    private Predicate<String> paramPredicate = param -> {
        String paramLowerCase = param.toLowerCase();
        return paramLowerCase.toLowerCase().contains("token") ||
                paramLowerCase.toLowerCase().contains("secret") ||
                paramLowerCase.toLowerCase().contains("key") ||
                paramLowerCase.toLowerCase().contains("cvv") ||
                paramLowerCase.toLowerCase().contains("credit") ||
                paramLowerCase.toLowerCase().contains("pass") ||
                paramLowerCase.toLowerCase().contains("phone");
    };
}
