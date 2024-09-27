package ru.nesterov.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetForClientScheduleRequest {
    private String clientName;
    private LocalDateTime leftDate;
    private LocalDateTime rightDate;
}
