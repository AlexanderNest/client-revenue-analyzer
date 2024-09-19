package ru.nesterov.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserResponse {
    private String userId;
    private String mainCalendarId;
    private String cancelledCalendarId;
}
