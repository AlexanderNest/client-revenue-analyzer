package ru.nesterov.calendar.integration.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateEventDto {
    private String mainCalendar;
    private String summary;
    private String description;
    private String start;
    private String end;
    private EventStatus status;
    private Long clientId;

}
