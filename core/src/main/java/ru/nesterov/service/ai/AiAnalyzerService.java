package ru.nesterov.service.ai;

import ru.nesterov.service.dto.UserDto;

public interface AiAnalyzerService {
    String analyzeClients(UserDto userDto, String month);
}
