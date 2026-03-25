package ru.nesterov.core.service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ClientScheduleDto {
    private String clientName;
    private LocalDateTime eventStart;
    private LocalDateTime eventEnd;
    private boolean requiresShift;
}
