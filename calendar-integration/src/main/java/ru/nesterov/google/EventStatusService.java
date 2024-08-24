package ru.nesterov.google;

import ru.nesterov.dto.EventStatus;

public interface EventStatusService {
    EventStatus getEventStatus(String eventColorId);
}
