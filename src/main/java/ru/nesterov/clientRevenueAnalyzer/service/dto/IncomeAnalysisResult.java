package ru.nesterov.clientRevenueAnalyzer.service.dto;

import lombok.Data;

@Data
public class IncomeAnalysisResult {
    private double actualIncome;
    private double expectedIncoming;
    private double lostIncome;
}
