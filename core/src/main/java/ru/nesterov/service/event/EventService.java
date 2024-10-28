package ru.nesterov.service.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventExtensionDto;
import ru.nesterov.entity.Client;
import ru.nesterov.exception.AppException;
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
            throw new AppException("Пользователь с именем '" + eventDto.getSummary() + "' от даты " + eventDto.getStart() + " не найден в базе");
        }
        EventExtensionDto extension = eventDto.getEventExtensionDto();
        if (extension != null && extension.getIncome() != null) {
            return extension.getIncome();
        }
        return getEventDuration(eventDto) * client.getPricePerHour();
    }
    
    public double getEventDuration(EventDto eventDto) {
        Duration duration = Duration.between(eventDto.getStart(), eventDto.getEnd());
        return duration.toMinutes() / 60.0;
    }
}
