package ru.nesterov.google.mapper;

import com.google.api.services.calendar.model.Event;

public class EventMapper {

    public static Event mapEventToEvent(Event event) {

        return new Event()
                .setSummary(event.getSummary())
                .setDescription(event.getDescription())
                .setColorId(event.getColorId())
                .setStart(event.getStart())
                .setEnd(event.getEnd())
                .setAttendees(event.getAttendees());
    }
}
