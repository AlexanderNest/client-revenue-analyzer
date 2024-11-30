package ru.nesterov.util;

import org.junit.jupiter.api.Test;
import ru.nesterov.dto.EventExtensionDto;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlainTextMapperTest {

    @Test
    void fillFromAliasesString() {
        PlainTextMapper mapper = new PlainTextMapper();

        String data = "доход: 100\nкомментарий: опоздание\nзапланировано: да";

        EventExtensionDto eventExtensionDto = mapper.fillFromString(data, EventExtensionDto.class);
        assertNotNull(eventExtensionDto);
        assertArrayEquals();
    }
}