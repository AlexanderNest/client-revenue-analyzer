package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.controller.request.GetClientScheduleRequest;
import ru.nesterov.controller.response.ClientResponse;
import ru.nesterov.controller.response.EventScheduleResponse;
import ru.nesterov.exception.ClientIsAlreadyCreatedException;
import ru.nesterov.mapper.ClientMapper;
import ru.nesterov.service.user.UserService;
import ru.nesterov.service.client.ClientService;
import ru.nesterov.service.date.helper.MonthDatesPair;
import ru.nesterov.service.dto.ClientDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
public class ClientControllerImpl implements ClientController {
    private final ClientService clientService;
    private final UserService userService;

    public List<EventScheduleResponse> getClientSchedule(@RequestHeader(name = "X-username") String username, @RequestBody GetClientScheduleRequest request) {
        List<MonthDatesPair> clientSchedule = clientService.getClientSchedule(userService.getUserByUsername(username), request.getClientName(), request.getLeftDate(), request.getRightDate());

        return clientSchedule.stream()
                .map(monthDatesPair -> new EventScheduleResponse(monthDatesPair.getFirstDate(), monthDatesPair.getLastDate()))
                .toList();
    }

    public ResponseEntity<ClientResponse> createClient(@RequestHeader(name = "X-username") String username, @RequestBody CreateClientRequest createClientRequest) {
        ClientDto clientDto = ClientMapper.mapToClientDto(createClientRequest);
        try {
            ClientDto result = clientService.createClient(userService.getUserByUsername(username), clientDto, createClientRequest.isIdGenerationNeeded());
            ClientResponse response = ClientMapper.mapToClientResponse(result);
            return ResponseEntity.ok(response);
        } catch (ClientIsAlreadyCreatedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Override
    public List<ClientResponse> getActiveClients(@RequestHeader(name = "X-username") String username) {
        List<ClientDto> activeClients = clientService.getActiveClients(userService.getUserByUsername(username));
        List<EventScheduleResponse> eventListSchedule;

        Map<ClientDto, Integer> mapClientAndPricePerMonth = new HashMap<>();
        GetClientScheduleRequest request = new GetClientScheduleRequest();
        request.setLeftDate(LocalDateTime.now());
        request.setRightDate(LocalDateTime.now().plusMonths(1));
        int hours = 0;
        int pricePerMonth = 0;
        for(ClientDto clientDto: activeClients) {
            request.setClientName(clientDto.getName());
            eventListSchedule = getClientSchedule(username, request);
            for(EventScheduleResponse eventScheduleResponse: eventListSchedule) {
                hours += (int) Duration.between(eventScheduleResponse.getEventStart(), eventScheduleResponse.getEventEnd()).toHours();
            }
            pricePerMonth = hours * clientDto.getPricePerHour();
            mapClientAndPricePerMonth.put(clientDto, pricePerMonth);
        }

        return mapClientAndPricePerMonth.entrySet().stream()
                .sorted(Map.Entry.<ClientDto, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .map(ClientMapper::mapToClientResponse)
                .collect(Collectors.toList());


//        return activeClients.stream()
//                .sorted(Comparator.comparing(ClientDto::getPricePerHour))
//                .map(ClientMapper::mapToClientResponse)
//                .toList();
    }
}
