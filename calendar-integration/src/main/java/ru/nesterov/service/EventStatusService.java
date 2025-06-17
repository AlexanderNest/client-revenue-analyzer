package ru.nesterov.service;

import ru.nesterov.dto.EventStatus;
import ru.nesterov.dto.PrimaryEventData;

public interface EventStatusService {
    EventStatus getEventStatus(PrimaryEventData primaryEventData);
}
