package ru.nesterov;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.nesterov.controller.request.CreateClientRequest;
import ru.nesterov.entity.Client;


@RequiredArgsConstructor
@RestController
public class ClientController {
    public Client createClient(CreateClientRequest createClientRequest) {
        return null;
    }
}
