package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
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

    public List<EventScheduleResponse> getClientSchedule(@RequestBody GetClientScheduleRequest request) {
        return clientService.getClientSchedule(request.getClientName(), request.getLeftDate(), request.getRightDate()).stream()
                .map(monthDatesPair -> new EventScheduleResponse(monthDatesPair.getFirstDate(), monthDatesPair.getLastDate()))
                .toList();
    }

    public ClientResponse createClient(@RequestBody CreateClientRequest createClientRequest) {
        ClientDto clientDto = ClientMapper.mapToClientDto(createClientRequest);
        ClientDto result = clientService.createClient(clientDto, createClientRequest.isIdGenerationNeeded());
        return ClientMapper.mapToClientResponse(result);
    }

    @Override
    public List<ClientResponse> getActiveClients() {
        return clientService.getActiveClients().stream()
                .map(ClientMapper::mapToClientResponse)
                .toList();
    }
}
