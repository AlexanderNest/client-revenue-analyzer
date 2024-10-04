package ru.nesterov.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetClientScheduleResponse {
    private LocalDateTime eventStart;
    private LocalDateTime eventEnd;
}
