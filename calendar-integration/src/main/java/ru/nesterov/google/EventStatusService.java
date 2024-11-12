package ru.nesterov.google;

import com.google.api.services.calendar.model.Event;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventStatus;

public interface EventStatusService {
    EventStatus getEventStatus(Event event);
    EventStatus setShiftedEventStatus(EventDto eventDto);
}
