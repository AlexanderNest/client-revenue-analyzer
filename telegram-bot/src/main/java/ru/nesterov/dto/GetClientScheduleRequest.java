package ru.nesterov.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class GetClientScheduleRequest {
    private Long userId;
    private String clientName;
    private LocalDate displayedMonth;
    private LocalDate firstDate;
    private LocalDate secondDate;

    public boolean isFilled() {
        return userId != null
                && clientName != null
                && displayedMonth != null
                && firstDate != null
                && secondDate != null;
    }
}