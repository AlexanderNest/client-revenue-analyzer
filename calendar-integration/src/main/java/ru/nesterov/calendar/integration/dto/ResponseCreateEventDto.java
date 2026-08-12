package ru.nesterov.calendar.integration.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResponseCreateEventDto {
    private String eventId;
    private String summary;
}
