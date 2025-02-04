package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.GetHolidaysRequest;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.CalendarType;
import ru.nesterov.google.GoogleCalendarService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class HolidayControllerImpl implements HolidayController {
    private final GoogleCalendarService googleCalendarService;

    public List<EventDto> getHolidays(@RequestBody GetHolidaysRequest getHolidaysRequest) {
        LocalDateTime leftDate = LocalDateTime.parse(getHolidaysRequest.getLeftDateStr(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        LocalDateTime rightDate = LocalDateTime.parse(getHolidaysRequest.getRightDateStr(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return googleCalendarService.getHolidays(leftDate, rightDate);
    }
}
