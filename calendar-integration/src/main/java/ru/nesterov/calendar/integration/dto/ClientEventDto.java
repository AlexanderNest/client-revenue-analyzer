package ru.nesterov.calendar.integration.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ClientEventDto {
    private Long clientId;
    private String eventId;
}
