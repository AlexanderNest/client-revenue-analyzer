package ru.nesterov.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String mainCalendarId;
    private String cancelledCalendarId;
    private String userIdentifier;
}
