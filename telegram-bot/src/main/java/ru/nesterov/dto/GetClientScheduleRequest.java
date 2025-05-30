package ru.nesterov.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetClientScheduleRequest {
    private Long userId;
    private String clientName;
    private LocalDate displayedMonth;
    private LocalDate firstDate;
    private LocalDate secondDate;
}