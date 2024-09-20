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
}
