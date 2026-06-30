package ru.nesterov.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.core.HeaderFilters;
import org.zalando.logbook.core.QueryFilters;
import org.zalando.logbook.json.JsonBodyFilters;

import java.util.Set;
import java.util.function.Predicate;

import static org.zalando.logbook.core.HeaderFilters.defaultValue;

@Configuration
public class LogbookSecurityConfig {

    private static final String REPLACEMENT = "<masked>";
    private static final Set<String> SENSITIVE_DATA_FIELDS = Set.of("token", "secret", "key", "pass");

    @Bean
    public Logbook logbook() {
        return Logbook.builder()
                .headerFilter(defaultValue())
                .headerFilter(HeaderFilters.replaceHeaders(paramPredicate, REPLACEMENT))
                .queryFilter(QueryFilters.replaceQuery(paramPredicate, REPLACEMENT))
                .bodyFilter(JsonBodyFilters.replaceJsonStringProperty(paramPredicate, REPLACEMENT))
                .build();
    }

    private final Predicate<String> paramPredicate = param -> {
        String paramLowerCase = param.toLowerCase();
        return SENSITIVE_DATA_FIELDS.stream().anyMatch(paramLowerCase::contains);
    };
}
