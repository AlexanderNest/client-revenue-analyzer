package ru.nesterov.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserRequest {
    private String mainCalendarId;
    private String cancelledCalendarId;
    private String userIdentifier;
    private Boolean isCancelledCalendarEnabled;

    public boolean isFilled() {
        boolean isCalendarEnabled = isCancelledCalendarEnabled != null && isCancelledCalendarEnabled && cancelledCalendarId != null;
        boolean isCalendarDisabled = isCancelledCalendarEnabled != null && !isCancelledCalendarEnabled && cancelledCalendarId == null;
        return mainCalendarId != null
                && (isCalendarDisabled || isCalendarEnabled)
                && userIdentifier != null;
    }
}
