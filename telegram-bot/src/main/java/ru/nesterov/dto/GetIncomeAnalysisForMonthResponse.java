package ru.nesterov.dto;

import lombok.Data;

@Data
public class GetIncomeAnalysisForMonthResponse {
    private double actualIncome;
    private double expectedIncome;
    private double lostIncome;
    private double potentialIncome;
}
