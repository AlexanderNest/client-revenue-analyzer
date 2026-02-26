package ru.nesterov.web.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserResponse {
    private long id;
    private String userIdentifier;
    private String mainCalendarId;
    private String cancelledCalendarId;
    private boolean isCancelledCalendarEnabled;
}
