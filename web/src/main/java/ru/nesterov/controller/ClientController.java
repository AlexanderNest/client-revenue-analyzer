package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.controller.response.CreateClientResponse;
import ru.nesterov.mapper.ControllerMapper;
import ru.nesterov.service.client.ClientService;

@RestController
@RequestMapping("/client/analyzer")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;
    private final ControllerMapper mapper;

    @PostMapping("/create-client")
    public CreateClientResponse createClient(@RequestBody CreateClientRequest createClientRequest) {
        return mapper.mapToCreateClientResponse(clientService.createClient(mapper.mapToClientDto(createClientRequest), createClientRequest.isGenerationOfIdNeeded()));
    }
}
