package ru.nesterov.calendar.integration.dto;

public enum EventStatus {
    SUCCESS("Оплачено", false),
    REQUIRES_SHIFT("Требуется перенос", false),
    PLANNED("Запланировано", false),
    PLANNED_CANCELLED("Запланированная отмена", true),
    UNPLANNED_CANCELLED("Незапланированная отмена", true);

    private final String description;
    private final boolean isCancelledStatus;

    EventStatus(String description, boolean isCancelledStatus) {
        this.description = description;
        this.isCancelledStatus = isCancelledStatus;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCancelledStatus() {
        return isCancelledStatus;
    }
}
