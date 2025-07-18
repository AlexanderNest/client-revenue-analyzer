package ru.nesterov.core.service.dto;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
@Builder
@Data
public class ClientScheduleDto {
    private LocalDateTime eventStart;
    private LocalDateTime eventEnd;
    private boolean approveRequires;
}
