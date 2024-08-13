package ru.nesterov.dto;

import lombok.Data;

@Data
public class GetIncomeAnalysisForMonthResponse {
    private double actualIncome;
    private double expectedIncoming;
    private double lostIncome;
}
