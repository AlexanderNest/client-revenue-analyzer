package ru.nesterov.web.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import java.util.List;

@Data
@RequiredArgsConstructor
@Schema(description = "Отчет с расписанием клиента: имя клиента и список событий")
public class ClientScheduleResponse {
    @Schema(description = "Имя клиента")
    private final String clientName;

    @Schema(description = "Список событий в расписании")
    private final List<EventScheduleResponse> events;
}
