package ru.nesterov.dto;

import lombok.Data;

import java.util.Map;

@Data
public class GetYearBusynessStatisticsResponse {
    private Map<String, Double> months;
    private Map<String, Double> days;
}
