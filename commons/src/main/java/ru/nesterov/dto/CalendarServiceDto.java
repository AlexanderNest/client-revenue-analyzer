package ru.nesterov.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class CalendarServiceDto {
    private String mainCalendar;
    private String cancelledCalendar;
    private LocalDateTime leftDate;
    private LocalDateTime rightDate;
}
