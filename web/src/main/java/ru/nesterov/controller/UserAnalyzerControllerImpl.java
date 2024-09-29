package ru.nesterov.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.nesterov.controller.request.GetForYearRequest;
import ru.nesterov.controller.response.YearBusynessStatisticsResponse;

public class UserAnalyzerControllerImpl{
    public YearBusynessStatisticsResponse getYearStatistics(@RequestHeader(name = "X-username") String username, @RequestBody GetForYearRequest getForYearRequest) {
        return null;
    }
}
