package ru.nesterov.entity.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;
import ru.nesterov.entity.EventExtension;

@Converter
@Slf4j
public class EventExtensionAttributeConverter implements AttributeConverter<EventExtension, String> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    @Override
    public String convertToDatabaseColumn(EventExtension attribute) {
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.warn("Не получилось сконвертировать EventExtension в JSON");
            return null;
        }
    }
    
    @Override
    public EventExtension convertToEntityAttribute(String dbData) {
        try {
            return OBJECT_MAPPER.readValue(dbData, EventExtension.class);
        } catch (JsonProcessingException e) {
            log.warn("Не получилось сконвертировать JSON в EventExtension");
            return null;
        }
    }
}
