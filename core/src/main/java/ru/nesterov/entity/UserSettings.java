package ru.nesterov.entity;

import lombok.Data;

@Data
public class UserSettings {
    private boolean isCancelledCalendarEnabled;
    private boolean isEventsBackupEnabled;
}
