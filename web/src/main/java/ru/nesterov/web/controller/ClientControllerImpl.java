package ru.nesterov.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.core.service.client.ClientService;
import ru.nesterov.core.service.date.helper.MonthDatesPair;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.ClientScheduleDto;
import ru.nesterov.core.service.user.UserService;
import ru.nesterov.web.controller.request.CreateClientRequest;
import ru.nesterov.web.controller.request.GetClientScheduleRequest;
import ru.nesterov.web.controller.response.ClientResponse;
import ru.nesterov.web.controller.response.EventScheduleResponse;
import ru.nesterov.web.mapper.ClientMapper;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClientControllerImpl implements ClientController {
    private final ClientService clientService;
    private final UserService userService;

    public List<EventScheduleResponse> getClientSchedule(@RequestHeader(name = "X-username") String username, @RequestBody GetClientScheduleRequest request) {
        List<ClientScheduleDto> clientSchedule = clientService.getClientSchedule(userService.getUserByUsername(username), request.getClientName(), request.getLeftDate(), request.getRightDate());

        return clientSchedule.stream()
                .map(dto -> new EventScheduleResponse(dto.getEventStart(), dto.getEventEnd(), dto.isApproveRequires()))
                .toList();
    }

    public ClientResponse createClient(
            @RequestHeader("X-username") String username,
            @RequestBody CreateClientRequest createClientRequest) {

        ClientDto dto = ClientMapper.mapToClientDto(createClientRequest);
        ClientDto saved = clientService.createClient(
                userService.getUserByUsername(username),
                dto,
                createClientRequest.isIdGenerationNeeded()
        );
        return ClientMapper.mapToClientResponse(saved);
    }

    @Override
    public List<ClientResponse> getActiveClients(@RequestHeader(name = "X-username") String username) {
        List<ClientDto> activeClients = clientService.getActiveClientsOrderedByPrice(userService.getUserByUsername(username));

        return activeClients.stream()
                .map(ClientMapper::mapToClientResponse)
                .toList();
    }
}
