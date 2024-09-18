package ru.nesterov.service.event;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.Event;
import ru.nesterov.dto.EventExtension;
import ru.nesterov.entity.Client;
import ru.nesterov.exception.AppException;
import ru.nesterov.repository.ClientRepository;

import java.time.Duration;

@Service
@Slf4j
@AllArgsConstructor
public class EventService {
    private final ClientRepository clientRepository;

    public double getEventIncome(Event event) {
        Client client = clientRepository.findClientByNameAndUserId(event.getSummary(), event.getUserId());
        if (client == null) {
            throw new AppException("Пользователь с именем '" + event.getSummary() + "' от даты " + event.getStart() + " не найден в базе");
        }
        EventExtension extension = event.getEventExtension();
        if (extension != null && extension.getIncome() != null) {
            return extension.getIncome();
        }
        return getEventDuration(event) * client.getPricePerHour();
    }

    public double getEventDuration(Event event) {
        Duration duration = Duration.between(event.getStart(), event.getEnd());
        return duration.toMinutes() / 60.0;
    }
}
