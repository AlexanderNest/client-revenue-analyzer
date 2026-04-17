package ru.nesterov.core.service.dto;

import lombok.Builder;
import lombok.Value;
import ru.nesterov.calendar.integration.dto.EventDto;
import ru.nesterov.core.entity.Client;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class PdfReportDataDto {
    ClientMeetingsStatistic stats;
    List<EventDto> events;
    Client client;
    LocalDateTime start;
    LocalDateTime end;
}
