package ru.nesterov.core.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.core.entity.ClientEvent;
import ru.nesterov.core.repository.ClientEventRepository;
import ru.nesterov.calendar.integration.dto.ClientEventDto;

@Service
@RequiredArgsConstructor
public class ClientEventServiceImpl implements ClientEventService {
    private final ClientEventRepository clientEventRepository;

    @Transactional
    @Override
    public ClientEventDto createRelation(ClientEventDto clientEventDto) {
        ClientEvent clientEvent = clientEventRepository.save(converterClientEventDtoToClientEvent(clientEventDto));
        return converterClientEventToClientEventDto(clientEvent);
    }

    private ClientEvent converterClientEventDtoToClientEvent(ClientEventDto clientEventDto) {
        ClientEvent clientEvent = new ClientEvent();
        clientEvent.setClientId(clientEvent.getClientId());
        clientEvent.setEventId(clientEventDto.getEventId());

        return clientEvent;
    }

    private ClientEventDto converterClientEventToClientEventDto(ClientEvent c) {
        return ClientEventDto.builder()
                .clientId(c.getClientId())
                .eventId(c.getEventId())
                .build();
    }

}
