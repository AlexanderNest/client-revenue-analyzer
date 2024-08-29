package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.controller.request.GetClientScheduleRequest;
import ru.nesterov.controller.response.ClientResponse;
import ru.nesterov.controller.response.EventScheduleResponse;
import ru.nesterov.mapper.ClientMapper;
import ru.nesterov.service.client.ClientService;
import ru.nesterov.service.dto.ClientDto;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClientControllerImpl implements ClientController {
    private final ClientService clientService;

    public List<EventScheduleResponse> getClientSchedule(@RequestHeader(name = "username") String username, @RequestBody GetClientScheduleRequest request) {
        return clientService.getClientSchedule(username, request.getClientName(), request.getLeftDate(), request.getRightDate()).stream()
                .map(monthDatesPair -> new EventScheduleResponse(monthDatesPair.getFirstDate(), monthDatesPair.getLastDate()))
                .toList();
    }

    public ClientResponse createClient(@RequestHeader(name = "username") String username, @RequestBody CreateClientRequest createClientRequest) {
        ClientDto clientDto = ClientMapper.mapToClientDto(createClientRequest);
        ClientDto result = clientService.createClient(username, clientDto, createClientRequest.isIdGenerationNeeded());
        return ClientMapper.mapToClientResponse(result);
    }

    @Override
    public List<ClientResponse> getActiveClients(@RequestHeader(name = "username") String username) {
        return clientService.getActiveClients(username).stream()
                .map(ClientMapper::mapToClientResponse)
                .toList();
    }
}
