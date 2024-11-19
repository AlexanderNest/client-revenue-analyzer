package ru.nesterov.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventExtensionDto {
    private String comment;
    private Integer income;
    private Boolean isPlanned;
    private LocalDateTime previousDate;
}
