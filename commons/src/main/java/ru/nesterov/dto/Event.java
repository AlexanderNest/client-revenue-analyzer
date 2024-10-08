package ru.nesterov.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class Event {
    private EventStatus status;
    private String summary;
    private LocalDateTime start;
    private LocalDateTime end;
    private EventExtension eventExtension;
}

