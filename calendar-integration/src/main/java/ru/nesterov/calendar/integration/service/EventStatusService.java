package ru.nesterov.calendar.integration.service;

import ru.nesterov.calendar.integration.dto.EventStatus;
import ru.nesterov.calendar.integration.dto.PrimaryEventData;

public interface EventStatusService {
    EventStatus getEventStatus(PrimaryEventData primaryEventData);
}
