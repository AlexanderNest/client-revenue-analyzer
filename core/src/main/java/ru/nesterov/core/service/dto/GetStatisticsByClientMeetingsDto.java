package ru.nesterov.core.service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GetStatisticsByClientMeetingsDto {
    private UserDto userDto;
    private String clientName;
    private LocalDateTime leftDate;
    private LocalDateTime rightDate;
}
