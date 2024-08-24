package ru.nesterov.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.controller.request.GetClientScheduleRequest;
import ru.nesterov.controller.response.CreateClientResponse;
import ru.nesterov.controller.response.EventScheduleResponse;

import java.util.List;

@Api(tags = "Управление клиентами")
@RequestMapping("/client")
public interface ClientController {

    @ApiOperation(value = "Получить расписание клиента", notes = "Возвращает расписание событий для указанного клиента")
    @PostMapping("/getSchedule")
    List<EventScheduleResponse> getClientSchedule(@RequestBody GetClientScheduleRequest request);

    @ApiOperation(value = "Создать клиента", notes = "Создает нового клиента и возвращает информацию о нем")
    @PostMapping("/create")
    CreateClientResponse createClient(@RequestBody CreateClientRequest createClientRequest);
}
