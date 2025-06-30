package ru.nesterov.core.service.dto;

import lombok.Data;

import java.util.Map;
@Data
public class BusynessAnalysisResult {
    private Map<String, Double> months;
    private Map<String, Double> days;
}
