package ru.nesterov.controller.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetUserResponse {
    private long userId;
    private String username;
    private String mainCalendarId;
    private Boolean isCancelledCalendarEnabled;
    private String cancelledCalendarId;
}
