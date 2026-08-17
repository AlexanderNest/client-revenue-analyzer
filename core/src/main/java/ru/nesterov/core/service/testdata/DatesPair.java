package ru.nesterov.core.service.testdata;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatesPair {
    private String startDate;
    private String endDate;
}
