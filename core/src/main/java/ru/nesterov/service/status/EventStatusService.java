package ru.nesterov.service.status;

import ru.nesterov.service.dto.EventStatus;

public interface EventStatusService {
    EventStatus getEventStatus(String eventColorId);
}
