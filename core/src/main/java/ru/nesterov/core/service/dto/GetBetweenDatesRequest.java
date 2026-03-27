package ru.nesterov.core.service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetBetweenDatesRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
