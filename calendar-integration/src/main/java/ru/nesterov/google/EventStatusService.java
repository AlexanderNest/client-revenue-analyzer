package ru.nesterov.google;

import com.google.api.services.calendar.model.Event;
import ru.nesterov.common.dto.EventStatus;

public interface EventStatusService {
    EventStatus getEventStatus(Event event);
}
