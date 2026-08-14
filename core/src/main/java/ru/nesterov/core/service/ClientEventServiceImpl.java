package ru.nesterov.core.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.core.entity.ClientEvent;
import ru.nesterov.core.repository.ClientEventRepository;
import ru.nesterov.calendar.integration.dto.ClientEventDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientEventServiceImpl implements ClientEventService {
    private final ClientEventRepository clientEventRepository;

    @Transactional
    @Override
    public ClientEventDto createClientEventLink(ClientEventDto clientEventDto) {
        ClientEvent clientEvent = clientEventRepository.save(converterClientEventDtoToClientEvent(clientEventDto));
        return converterClientEventToClientEventDto(clientEvent);
    }

    @Override
    public List<String> getEventIdsByClientId(Long clientId) {
        return clientEventRepository.getEventIdsByClientId(clientId);
    }

    private ClientEvent converterClientEventDtoToClientEvent(ClientEventDto clientEventDto) {
        ClientEvent clientEvent = new ClientEvent();
        clientEvent.setClientId(clientEventDto.getClientId());
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
