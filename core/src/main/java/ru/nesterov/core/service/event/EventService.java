package ru.nesterov.core.service.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.calendar.integration.dto.EventExtensionDto;
import ru.nesterov.core.entity.Client;
import ru.nesterov.core.entity.PriceChangeHistory;
import ru.nesterov.core.exception.ClientNotFoundException;
import ru.nesterov.core.repository.ClientRepository;
import ru.nesterov.core.service.dto.UserDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@Slf4j
@AllArgsConstructor
public class EventService {
    private final ClientRepository clientRepository;
    
    public double getEventIncome(Client client, EventDto eventDto) {
        if (client == null) {
            throw new ClientNotFoundException(eventDto.getSummary(), eventDto.getStart());
        }

        EventExtensionDto extension = eventDto.getEventExtensionDto();
        if (extension != null && extension.getIncome() != null) {
            return extension.getIncome();
        }

        return getEventDuration(eventDto) * getPricePerHourForDate(client, eventDto.getStart());
    }

    public double getPricePerHourForDate(Client client, LocalDateTime dateTime) {
        if (client.getPriceChangeHistory().isEmpty()) {
            return client.getPricePerHour();
        }

        return client.getPriceChangeHistory().stream()
                .filter(pch -> pch.getChangeDate().isAfter(dateTime))
                .min(Comparator.comparing(PriceChangeHistory::getChangeDate))
                .map(PriceChangeHistory::getPreviousPrice)
                .orElseThrow(() -> new RuntimeException("Something goes wrong while calculating a price for an event"));
    }


    public double getEventDuration(EventDto eventDto) {
        if (eventDto.getStart().isAfter(eventDto.getEnd())) {
            throw new IllegalArgumentException();
        }
        Duration duration = Duration.between(eventDto.getStart(), eventDto.getEnd());
        return duration.toMinutes() / 60.0;
    }
}
