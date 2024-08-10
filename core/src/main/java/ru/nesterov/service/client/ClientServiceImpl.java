package ru.nesterov.service.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.nesterov.exception.AppException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.service.dto.ClientDto;
import ru.nesterov.service.mapper.ClientMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final ClientMapper mapper;

    public ClientDto createClient(ClientDto clientDto, boolean isGenerationOfIdNeeded) throws AppException {
        List<ClientDto> clientsWithThisName = findAllByNameContaining(clientDto.getName());
        if (clientsWithThisName.isEmpty()) {
            return mapper.mapToClientDto(clientRepository.saveAndFlush(mapper.mapToClient(clientDto)));
        } else if (isGenerationOfIdNeeded) {
            clientDto.setName(clientDto.getName() + generateUniqueId(clientsWithThisName));
                return mapper.mapToClientDto(clientRepository.saveAndFlush(mapper.mapToClient(clientDto)));
        } else {
            throw new AppException("Клиент с таким именем уже существует");
        }
    }

    public List<ClientDto> findAllByNameContaining(String name) {
        return clientRepository.findAllByNameContaining(name).stream()
                .map(mapper::mapToClientDto)
                .toList();
    }

    private long generateUniqueId(List<ClientDto> clients) {
        List<String> names = clients.stream()
                .map(ClientDto::getName)
                .toList();
        if (names.size() == 1) {
            return 2;
        } else {
            return Long.parseLong(names.get(names.size() - 1).replace(names.get(0), "")) + 1;
        }
    }
}
