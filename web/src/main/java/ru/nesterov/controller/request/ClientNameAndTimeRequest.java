package ru.nesterov.controller.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientNameAndTimeRequest {
    private String clientName;
    private LocalDateTime leftDate;
    private LocalDateTime rightDate;
}
