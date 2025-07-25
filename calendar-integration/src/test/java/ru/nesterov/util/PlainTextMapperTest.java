package ru.nesterov.util;

import org.junit.jupiter.api.Test;
import ru.nesterov.calendar.integration.dto.EventExtensionDto;
import ru.nesterov.calendar.integration.util.PlainTextMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PlainTextMapperTest {

    @Test
    void fillFromAliasesStringV1() {
        String data = "доход: 100\nкомментарий: опоздание\nзапланировано: да\nпредыдущая дата: 12.11.2024";

        EventExtensionDto eventExtensionDto = PlainTextMapper.fillFromString(data, EventExtensionDto.class);
        assertNotNull(eventExtensionDto);
        assertEquals(eventExtensionDto.getComment(), "опоздание");
        assertEquals(eventExtensionDto.getIncome(), 100);
        assertEquals(eventExtensionDto.getIsPlanned(), true);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        assertEquals(LocalDateTime.parse("12.11.2024 00:00", formatter), eventExtensionDto.getPreviousDate());
    }

    @Test
    void fillFromAliasesStringV2() {
        String data = "доход    :    100     \n      комментарий :опоздание\nзапланировано:            да\n      предыдущая дата                              : 12.11.2024";

        EventExtensionDto eventExtensionDto = PlainTextMapper.fillFromString(data, EventExtensionDto.class);
        assertNotNull(eventExtensionDto);
        assertEquals(eventExtensionDto.getComment(), "опоздание");
        assertEquals(eventExtensionDto.getIncome(), 100);
        assertEquals(eventExtensionDto.getIsPlanned(), true);


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        assertEquals(LocalDateTime.parse("12.11.2024 00:00", formatter), eventExtensionDto.getPreviousDate());
    }
}