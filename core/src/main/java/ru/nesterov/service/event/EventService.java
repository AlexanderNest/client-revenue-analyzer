package ru.nesterov.service.event;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.nesterov.dto.Event;
import ru.nesterov.dto.EventExtension;
import ru.nesterov.entity.Client;
import ru.nesterov.exception.AppException;
import ru.nesterov.repository.ClientRepository;

import java.time.Duration;

@Service
@AllArgsConstructor
public class EventService {
    private final ClientRepository clientRepository;
    private final Logger logger = LoggerFactory.getLogger(EventService.class);

    public double getEventIncome(Event event) {
        Client client = clientRepository.findClientByName(event.getSummary());
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
