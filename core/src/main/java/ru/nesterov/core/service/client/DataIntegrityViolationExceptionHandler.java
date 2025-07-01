package ru.nesterov.core.service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DataIntegrityViolationExceptionHandler {
    private static final Map<String, String> INDEXES = Map.of(
            "IDX_UNIQUE_PHONE_PER_USER", "Номер телефона",
            "IDX_UNIQUE_CLIENT_NAME_PER_USER", "Имя клиента"
    );

    private static String getIndexName(DataIntegrityViolationException dataIntegrityViolationException) {
        Pattern pattern = Pattern.compile("\"(?:PUBLIC\\.)?([A-Za-z0-9_]+)(?:\\s+ON)?");
        Matcher matcher = pattern.matcher(dataIntegrityViolationException.getMessage());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static String getLocalizedMessage(DataIntegrityViolationException exception) {
        return INDEXES.get(getIndexName(exception));
    }
}
