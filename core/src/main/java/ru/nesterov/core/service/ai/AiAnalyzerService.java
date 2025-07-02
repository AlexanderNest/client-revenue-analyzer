package ru.nesterov.core.service.ai;

import ru.nesterov.core.service.dto.UserDto;

public interface AiAnalyzerService {
    String analyzeClients(UserDto userDto, String month);
}
