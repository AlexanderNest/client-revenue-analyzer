package ru.nesterov.service.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
@Data
public class BusynessAnalysisResult {
    private Map<String, Double> months;
    private Map<String, Double> days;
}
