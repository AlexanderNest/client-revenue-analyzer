package ru.nesterov.core.service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nesterov.core.repository.ClientBatchRepository;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientBatchService {
    private final ClientBatchRepository clientBatchRepository;

    public List<ClientDto> createClient(UserDto userDto, List<ClientDto> clientDtos) {
        return clientBatchRepository.createClient(userDto, clientDtos);
    }

    public void deleteClient() {
        // TODO DELETE
    }

}
