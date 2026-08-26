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
    public List<ClientEventDto> createClientEventLinks(List<ClientEventDto> clientEventDtoList) {
        List<ClientEvent> clientEvents = clientEventDtoList.stream()
                .map(this::converterClientEventDtoToClientEvent)
                .toList();

        return clientEventRepository.saveAll(clientEvents).stream()
                .map(this::converterClientEventToClientEventDto)
                .toList();
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

    private ClientEventDto converterClientEventToClientEventDto(ClientEvent clientEvent) {
        return ClientEventDto.builder()
                .clientId(clientEvent.getClientId())
                .eventId(clientEvent.getEventId())
                .build();
    }
}
