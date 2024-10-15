package ru.nesterov.mapper;

import ru.nesterov.controller.response.YearBusynessStatisticsResponse;
import ru.nesterov.service.dto.BusynessAnalysisResult;


public class UserMapper {
    public static YearBusynessStatisticsResponse mapToResponse(BusynessAnalysisResult busynessAnalysisResult) {
        YearBusynessStatisticsResponse response = new YearBusynessStatisticsResponse();
        response.setDays(busynessAnalysisResult.getDays());
        response.setMonths(busynessAnalysisResult.getMonths());
        return response;
    }
}
