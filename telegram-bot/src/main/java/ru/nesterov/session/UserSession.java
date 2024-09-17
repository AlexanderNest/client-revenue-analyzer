package ru.nesterov.session;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Builder
@Getter
@Setter
public class UserSession {
    private Long chatId;
    private String clientName;
    private LocalDate firstDate;
    private LocalDate secondDate;
    private SessionState state;
}
