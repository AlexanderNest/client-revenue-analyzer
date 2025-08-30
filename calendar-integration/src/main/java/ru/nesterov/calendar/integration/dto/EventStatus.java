package ru.nesterov.calendar.integration.dto;

public enum EventStatus {
    SUCCESS,
    REQUIRES_SHIFT,
    PLANNED,
    PLANNED_CANCELLED,
    UNPLANNED_CANCELLED;

    public boolean isCancelledStatus() {
        return this == PLANNED_CANCELLED || this == UNPLANNED_CANCELLED;
    }
}
