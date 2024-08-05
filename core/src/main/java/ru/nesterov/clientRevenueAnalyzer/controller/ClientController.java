package ru.nesterov.clientRevenueAnalyzer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.clientRevenueAnalyzer.controller.request.CreateClientRequest;
import ru.nesterov.clientRevenueAnalyzer.entity.Client;


@RequiredArgsConstructor
@RestController
public class ClientController {
    public Client createClient(CreateClientRequest createClientRequest) {
        return null;
    }
}
