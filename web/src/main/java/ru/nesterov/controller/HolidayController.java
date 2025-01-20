package ru.nesterov.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.CalendarType;
import ru.nesterov.google.GoogleCalendarService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
public class HolidayController {
    private GoogleCalendarService googleCalendarService;
    @GetMapping("/holidayDay")
    public List<EventDto> getHolidayDays() {
        String calendarId = "ru.russian#holiday@group.v.calendar.google.com";
        String leftDateStr = "2024-12-12 15:00";
        LocalDateTime leftDate = LocalDateTime.parse(leftDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String rightDateStr = "2024-31-12 21:00";
        LocalDateTime rightDate = LocalDateTime.parse(rightDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        return googleCalendarService.holidayDays(calendarId, CalendarType.MAIN, leftDate, rightDate);
    }

}
