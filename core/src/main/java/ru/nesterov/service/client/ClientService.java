package ru.nesterov.service.client;

import ru.nesterov.entity.Client;
import ru.nesterov.service.monthHelper.MonthDatesPair;

import java.time.LocalDateTime;
import java.util.List;

public interface ClientService {
    List<MonthDatesPair> getClientSchedule(String clientName, LocalDateTime leftDate, LocalDateTime rightDate);
    List<Client> getFilteredByPriceClients(boolean active);
}
