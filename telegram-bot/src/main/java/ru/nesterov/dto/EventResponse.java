package ru.nesterov.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventResponse {
    private String summary;
    private LocalDateTime eventStart;
}
