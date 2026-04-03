package ru.nesterov.core.service.mapper;

import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.PriceChangeHistory;
import ru.nesterov.core.service.dto.ClientDto;

import java.util.Comparator;


public class ClientMapper {
    public static ClientDto mapToClientDto(Client client) {
        Integer currentPrice = client.getPriceChangeHistory() == null ? 0 :
                client.getPriceChangeHistory().stream()
                        .max(Comparator.comparing(PriceChangeHistory::getChangeDate))
                        .map(PriceChangeHistory::getPrice)
                .orElse(0);

        return ClientDto.builder()
                .description(client.getDescription())
                .pricePerHour(currentPrice)
                .name(client.getName())
                .id(client.getId())
                .active(client.isActive())
                .startDate(client.getStartDate())
                .phone(client.getPhone())
                .build();
    }

    public static Client mapToClient(ClientDto clientDto) {
        Client client = new Client();
        client.setDescription(clientDto.getDescription());
        client.setId(clientDto.getId());
        client.setName(clientDto.getName());
        client.setActive(clientDto.isActive());
        client.setPhone(clientDto.getPhone());
        client.setStartDate(clientDto.getStartDate());
        return client;
    }
}
