package ru.nesterov.clientRevenueAnalyzer.service.dto;

import ru.nesterov.clientRevenueAnalyzer.dto.EventColor;

import java.util.List;

public enum EventStatus {
    SUCCESS(List.of(EventColor.SAGE, EventColor.BASIL, EventColor.BASIL2)),
    CANCELLED(List.of(EventColor.TOMATO)),
    REQUIRES_SHIFT(List.of(EventColor.BANANA)),
    PLANNED(List.of())
    ;

    private final List<EventColor> eventColors;

    EventStatus(List<EventColor> eventColors) {
        this.eventColors = eventColors;
    }

    public static EventStatus fromColor(EventColor color) {
        if (color == EventColor.DEFAULT) {
            return PLANNED;
        }

        for (EventStatus status : EventStatus.values()) {
            if (status.eventColors.contains(color)) {
                return status;
            }
        }

        return null;
    }
}
