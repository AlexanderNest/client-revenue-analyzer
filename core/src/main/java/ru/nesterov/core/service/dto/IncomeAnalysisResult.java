package ru.nesterov.core.service.dto;

import lombok.Data;

@Data
public class IncomeAnalysisResult {
    private double actualIncome;
    private double expectedIncome;
    private double lostIncome;
    private double potentialIncome;
    private double lostIncomeDueToHoliday;
}
