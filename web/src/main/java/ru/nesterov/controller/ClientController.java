package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.GetClientScheduleRequest;
import ru.nesterov.controller.response.EventScheduleResponse;
import ru.nesterov.service.client.ClientService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientController {
    private final ClientService clientService;

    @PostMapping("/getSchedule")
    public List<EventScheduleResponse> getClientSchedule(@RequestBody GetClientScheduleRequest request) {
        return clientService.getClientSchedule(request.getClientName(), request.getLeftDate(), request.getRightDate()).stream()
                .map(monthDatesPair -> new EventScheduleResponse(monthDatesPair.getFirstDate(), monthDatesPair.getLastDate()))
                .toList();
    }
}
