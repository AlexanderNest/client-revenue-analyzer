package ru.nesterov.service.holiday;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.dto.CalendarType;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventExtensionDto;
import ru.nesterov.dto.EventStatus;

import ru.nesterov.google.GoogleCalendarClient;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.service.dto.GetHolidaysRequest;

@SpringBootTest
@ContextConfiguration(classes = GoogleCalendarService.class)
public class HolidayMethodTest {
    @Autowired
    private GoogleCalendarService googleCalendarService;
    @MockBean
    private GoogleCalendarClient googleCalendarClient;

    @Value("${app.google.calendar.holiday.calendar}")
    private String calendarId;

    @Test
    public void testGetHolidays() {
        GetHolidaysRequest request = new GetHolidaysRequest();
        request.setLeftDateStr("2023-01-01 00:00");
        request.setRightDateStr("2023-01-31 23:59");

        LocalDateTime leftDate = LocalDateTime.parse(request.getLeftDateStr(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime rightDate = LocalDateTime.parse(request.getRightDateStr(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        EventExtensionDto eventExtensionDto = new EventExtensionDto();

        EventDto event1 = EventDto.builder()
                .status(EventStatus.SUCCESS)
                .summary("3000")
                .start(LocalDateTime.parse("2024-12-31 22:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .end(LocalDateTime.parse("2025-01-21 12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .eventExtensionDto(eventExtensionDto)
                .build();

        EventDto event2 = EventDto.builder()
                .status(EventStatus.PLANNED)
                .summary("2000")
                .start(LocalDateTime.parse("2024-11-31 22:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .end(LocalDateTime.parse("2025-02-21 12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .eventExtensionDto(eventExtensionDto)
                .build();

        List<EventDto> expectedEvents = Arrays.asList(event1, event2);

        when(googleCalendarClient.getEventsBetweenDates(calendarId, CalendarType.PLAIN, leftDate, rightDate)).thenReturn(expectedEvents);

        List<EventDto> eventDtos = googleCalendarService.getHolidays(leftDate, rightDate);

        Assertions.assertSame(eventDtos.get(0).getStatus(), EventStatus.SUCCESS);
        Assertions.assertEquals("3000", eventDtos.get(0).getSummary());
        Assertions.assertEquals(eventDtos.get(0).getStart(), LocalDateTime.parse("2024-12-31 22:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        Assertions.assertEquals(eventDtos.get(0).getEnd(), LocalDateTime.parse("2025-01-21 12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        Assertions.assertSame(eventDtos.get(0).getEventExtensionDto(), eventExtensionDto);

        Assertions.assertSame(eventDtos.get(1).getStatus(), EventStatus.PLANNED);
        Assertions.assertEquals("2000", eventDtos.get(1).getSummary());
        Assertions.assertEquals(eventDtos.get(1).getStart(), LocalDateTime.parse("2024-11-31 22:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        Assertions.assertEquals(eventDtos.get(1).getEnd(), LocalDateTime.parse("2025-02-21 12:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        Assertions.assertSame(eventDtos.get(1).getEventExtensionDto(), eventExtensionDto);
    }
}
