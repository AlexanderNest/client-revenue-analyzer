package ru.nesterov.core.service.ai;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import ru.nesterov.core.formatter.ClientAnalyticsFormatter;
import ru.nesterov.core.service.dto.ClientMeetingsStatistic;
import ru.nesterov.core.service.dto.UserDto;
import ru.nesterov.core.service.event.EventsAnalyzerService;
import ru.nesterov.ai.AIIntegrationService;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiAnalyzerServiceImpl implements AiAnalyzerService {
    private final AIIntegrationService AIIntegrationService;
    private final EventsAnalyzerService eventsAnalyzerService;

    private final String PROMPT_TEMPLATE;

    @SneakyThrows
    public AiAnalyzerServiceImpl(AIIntegrationService AIIntegrationService, EventsAnalyzerService eventsAnalyzerService, @Value("${giga-chat.prompt.path}") Resource promptTemplateResource) {
        this.AIIntegrationService = AIIntegrationService;
        this.eventsAnalyzerService = eventsAnalyzerService;
        this.PROMPT_TEMPLATE = promptTemplateResource.getContentAsString(StandardCharsets.UTF_8);
    }

    public String analyzeClients(UserDto userDto, String month) {
        Map<String, ClientMeetingsStatistic> meetingsStatistic = eventsAnalyzerService.getStatisticsOfEachClientMeetingsForMonth(userDto, month);
        Set<Map.Entry<String, ClientMeetingsStatistic>> entries = meetingsStatistic.entrySet()
                .stream()
                .filter(entry -> entry.getValue().isFilledStatistic())
                .collect(Collectors.toSet());

        String prompt = PROMPT_TEMPLATE.replace("{{ClientData}}", ClientAnalyticsFormatter.format(entries));
        return AIIntegrationService.generateText(prompt);
    }
}
