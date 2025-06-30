package ru.nesterov.web.controller.response;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class EventResponse {
    private String summary;
    private LocalDateTime eventStart;
}
