package ru.nesterov.calendar.integration.dto;

import lombok.Data;
import ru.nesterov.calendar.integration.annotation.FieldAlias;

import java.time.LocalDateTime;

@Data
public class EventExtensionDto {
    @FieldAlias("комментарий")
    private String comment;
    @FieldAlias("доход")
    private Integer income;
    @FieldAlias("запланировано")
    private Boolean isPlanned;
    @FieldAlias("предыдущая дата")
    private LocalDateTime previousDate;
}
