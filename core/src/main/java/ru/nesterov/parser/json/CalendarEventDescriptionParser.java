package ru.nesterov.parser.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CalendarEventDescriptionParser {
    private final ObjectMapper objectMapper;

    public <T> T parseField(String description, String fieldName, Class<T> targetClass) throws IOException {
        JsonNode rootNode = objectMapper.readTree(description);
        JsonNode fieldNode = rootNode.path(fieldName);

        if (fieldNode.isNull()) {
            return null;
        }

        return objectMapper.treeToValue(fieldNode, targetClass);
    }
}
