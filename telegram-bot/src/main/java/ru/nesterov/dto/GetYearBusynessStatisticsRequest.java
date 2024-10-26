package ru.nesterov.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetYearBusynessStatisticsRequest {
    private Long userId;
    private int year;
}
