package ru.nesterov.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventExtension {
    private String comment;
    private Integer income;
    private Boolean isPlanned;
    private LocalDateTime previousDate;
}
