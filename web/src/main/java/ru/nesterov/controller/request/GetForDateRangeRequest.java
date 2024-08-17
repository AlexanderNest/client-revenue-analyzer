package ru.nesterov.controller.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GetForDateRangeRequest {
    private LocalDateTime start;
    private LocalDateTime end;
}
