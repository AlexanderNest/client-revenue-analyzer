package ru.nesterov.web.controller.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@RequiredArgsConstructor
public class EventScheduleResponse {
    private final LocalDateTime eventStart;
    private final LocalDateTime eventEnd;
    private final boolean approveRequires;
}
