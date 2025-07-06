package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.event.EventsAnalyzerService;
import ru.nesterov.core.service.user.UserService;
import ru.nesterov.web.controller.request.GetForYearRequest;
import ru.nesterov.web.controller.response.YearBusynessStatisticsResponse;
import ru.nesterov.web.mapper.UserMapper;

@RestController
@RequiredArgsConstructor
public class UserAnalyzerControllerImpl implements UserAnalyzerController {
    private final EventsAnalyzerService eventsAnalyzerService;
    private final UserService userService;

    public YearBusynessStatisticsResponse getYearBusynessStatistics(@RequestHeader(name = "X-username") String username, @RequestBody GetForYearRequest getForYearRequest) {
        UserDto userDto = userService.getUserByUsername(username);
        return UserMapper.mapToResponse(eventsAnalyzerService.getBusynessStatisticsByYear(userDto, getForYearRequest.getYear()));
    }
}
