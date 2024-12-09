package ru.nesterov.mapper;

import ru.nesterov.controller.response.YearBusynessStatisticsResponse;
import ru.nesterov.service.dto.BusynessAnalysisResult;
import ru.nesterov.controller.response.GetUserResponse;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.dto.UserSettingsDto;

public class UserMapper {
    public static YearBusynessStatisticsResponse mapToResponse(BusynessAnalysisResult busynessAnalysisResult) {
        YearBusynessStatisticsResponse response = new YearBusynessStatisticsResponse();
        response.setDays(busynessAnalysisResult.getDays());
        response.setMonths(busynessAnalysisResult.getMonths());
        return response;
    }

    public UserDto mapToUserDto(CreateUserRequest request) {
        return UserDto.builder()
                .mainCalendar(request.getMainCalendarId())
                .userSettings(
                        UserSettingsDto.builder()
                                .isCancelledCalendarEnabled(request.getIsCancelledCalendarEnabled())
                        .build()
                )
                .username(request.getUserIdentifier())
                .cancelledCalendar(request.getCancelledCalendarId())
                .build();
    }

    public CreateUserResponse mapToCreateUserResponse(UserDto userDto) {
        return CreateUserResponse.builder()
                .id(userDto.getId())
                .userIdentifier(userDto.getUsername())
                .isCancelledCalendarEnabled(userDto.getUserSettings().isCancelledCalendarEnabled())
                .cancelledCalendarId(userDto.getCancelledCalendar())
                .mainCalendarId(userDto.getMainCalendar())
                .build();
    }

    public GetUserResponse mapToGetUserResponse(UserDto userDto) {
        if (userDto == null) {
            return null;
        }
        return GetUserResponse.builder()
                .userId(userDto.getId())
                .cancelledCalendarId(userDto.getCancelledCalendar())
                .mainCalendarId(userDto.getMainCalendar())
                .username(userDto.getUsername())
                .isCancelledCalendarEnabled(userDto.getUserSettings().isCancelledCalendarEnabled())
                .build();
    }
}
