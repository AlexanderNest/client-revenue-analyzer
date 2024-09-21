package ru.nesterov.session;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class UserData {
    private Long userId;
    private String clientName;
    private LocalDate currentDate;
    private LocalDate firstDate;
    private LocalDate secondDate;
}
