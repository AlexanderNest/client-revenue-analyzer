package ru.nesterov.service.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nesterov.common.dto.EventDto;
import ru.nesterov.common.dto.EventExtensionDto;
import ru.nesterov.entity.Client;
import ru.nesterov.exception.ClientNotFoundException;
import ru.nesterov.repository.ClientRepository;
import ru.nesterov.service.dto.UserDto;

import java.time.Duration;

@Service
@Slf4j
@AllArgsConstructor
public class EventService {
    private final ClientRepository clientRepository;
    
    public double getEventIncome(UserDto userDto, EventDto eventDto) {
        Client client = clientRepository.findClientByNameAndUserId(eventDto.getSummary(), userDto.getId());
        if (client == null) {
            throw new ClientNotFoundException(eventDto.getSummary(), eventDto.getStart());
        }
        EventExtensionDto extension = eventDto.getEventExtensionDto();
        if (extension != null && extension.getIncome() != null) {
            return extension.getIncome();
        }
        return getEventDuration(eventDto) * client.getPricePerHour();
    }
    
    public double getEventDuration(EventDto eventDto) {
        if (eventDto.getStart().isAfter(eventDto.getEnd())) {
            throw new IllegalArgumentException();
        }
        Duration duration = Duration.between(eventDto.getStart(), eventDto.getEnd());
        return duration.toMinutes() / 60.0;
    }
}
