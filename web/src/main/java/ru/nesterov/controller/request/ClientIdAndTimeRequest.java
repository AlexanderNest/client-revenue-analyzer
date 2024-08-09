package ru.nesterov.controller.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientIdAndTimeRequest {
    private Long clientId;
    private LocalDateTime leftDate;
    private LocalDateTime rightDate;
}
