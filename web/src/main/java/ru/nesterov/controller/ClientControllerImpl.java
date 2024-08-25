package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.controller.request.GetClientScheduleRequest;
import ru.nesterov.controller.response.CreateClientResponse;
import ru.nesterov.controller.response.EventScheduleResponse;
import ru.nesterov.controller.response.GetActiveClientsResponse;
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

    public CreateClientResponse createClient(@RequestBody CreateClientRequest createClientRequest) {
        ClientDto clientDto = ClientMapper.mapToClientDto(createClientRequest);
        ClientDto result = clientService.createClient(clientDto, createClientRequest.isIdGenerationNeeded());
        return ClientMapper.mapToCreateClientResponse(result);
    }

    @Override
    public List<GetActiveClientsResponse> getActiveClients() {
        return clientService.getActiveClients(true).stream()
                .map(client -> GetActiveClientsResponse.builder()
                        .description(client.getDescription())
                        .id(client.getId())
                        .name(client.getName())
                        .pricePerHour(client.getPricePerHour())
                        .active(client.isActive())
                        .build())
                .toList();
    }
}
