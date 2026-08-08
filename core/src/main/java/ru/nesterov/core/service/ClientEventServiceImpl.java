package ru.nesterov.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.core.repository.ClientEventRepository;
import ru.nesterov.calendar.integration.dto.ClientEventDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientEventServiceImpl implements ClientEventService {
    private final ClientEventRepository clientEventRepository;

    @Override
    public List<ClientEventDto> batchCreateRelation(List<ClientEventDto> clientEventDtosList) {
       return clientEventRepository.batchCreateRelation(clientEventDtosList);
    }

    @Override
    public void deleteRelation() {
        // TODO delete;
    }
}
