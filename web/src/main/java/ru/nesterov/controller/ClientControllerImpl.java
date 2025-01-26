package ru.nesterov.controller;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.MDC;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClientControllerImpl implements ClientController {
    private final ClientService clientService;
    private final UserService userService;
    private static final Logger logger = LogManager.getLogger(ClientControllerImpl.class);

    @Override
    public List<EventScheduleResponse> getClientSchedule(@RequestHeader(name = "X-username") String username, @RequestBody GetClientScheduleRequest request) {
        logger.info("Поступил запрос на получение расписания клиента от пользователя {}", username);
        logger.debug("Параметры запроса: {}", request);

        List<MonthDatesPair> clientSchedule = clientService.getClientSchedule(userService.getUserByUsername(username), request.getClientName(), request.getLeftDate(), request.getRightDate());

        List<EventScheduleResponse> response = clientSchedule.stream()
                .map(monthDatesPair -> new EventScheduleResponse(monthDatesPair.getFirstDate(), monthDatesPair.getLastDate()))
                .toList();

        logger.info("Ответ на запрос: {}", response);
        return response;
    }

    @Override
    public ResponseEntity<ClientResponse> createClient(@RequestHeader(name = "X-username") String username, @RequestBody CreateClientRequest createClientRequest) {
        logger.info("Поступил запрос на создание клиента от пользователя {}", username);
        logger.debug("Параметры запроса: {}", createClientRequest);

        ClientDto clientDto = ClientMapper.mapToClientDto(createClientRequest);

        try {
            ClientDto result = clientService.createClient(userService.getUserByUsername(username), clientDto, createClientRequest.isIdGenerationNeeded());
            ClientResponse response = ClientMapper.mapToClientResponse(result);
            logger.info("Клиент успешно создан: {}", response);
            return ResponseEntity.ok(response);
        } catch (ClientIsAlreadyCreatedException e) {
            logger.error("Ошибка при создании клиента: клиент уже существует", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @Override
    public List<ClientResponse> getActiveClients(@RequestHeader(name = "X-username") String username) {
        logger.info("Поступил запрос на получение списка активных клиентов от пользователя {}", username);

        List<ClientDto> activeClients = clientService.getActiveClients(userService.getUserByUsername(username));

        logger.info("Список активных клиентов: {}", activeClients);
        return activeClients.stream()
                .map(ClientMapper::mapToClientResponse)
                .toList();
    }
}
