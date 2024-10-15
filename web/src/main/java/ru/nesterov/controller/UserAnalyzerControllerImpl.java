package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.GetForYearRequest;
import ru.nesterov.controller.response.YearBusynessStatisticsResponse;
import ru.nesterov.mapper.UserMapper;
import ru.nesterov.service.UserService;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.event.EventsAnalyzerService;

@RestController
@RequiredArgsConstructor
public class UserAnalyzerControllerImpl implements UserAnalyzerController {
    private final EventsAnalyzerService eventsAnalyzerService;
    private final UserService userService;

    public YearBusynessStatisticsResponse getYearStatistics(@RequestHeader(name = "X-username") String username, @RequestBody GetForYearRequest getForYearRequest) {
        UserDto userDto = userService.getUserByUsername(username);
        return UserMapper.mapToResponse(eventsAnalyzerService.getBusynessStatisticsByYear(userDto, getForYearRequest.getYear()));
    }
}
