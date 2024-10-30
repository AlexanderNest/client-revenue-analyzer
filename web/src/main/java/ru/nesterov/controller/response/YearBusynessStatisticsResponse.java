package ru.nesterov.controller.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;
@Data
@RequiredArgsConstructor
public class YearBusynessStatisticsResponse {
    private Map<String, Double> months;
    private Map<String, Double> days;
}
