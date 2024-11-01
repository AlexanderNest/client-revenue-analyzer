package ru.nesterov.service.date.helper;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Data
public class MonthDatesPair {
    private final LocalDateTime firstDate;
    private final LocalDateTime lastDate;
}
