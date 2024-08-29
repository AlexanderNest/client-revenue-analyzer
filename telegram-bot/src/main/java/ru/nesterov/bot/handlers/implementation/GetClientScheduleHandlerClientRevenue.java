package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.dto.GetClientScheduleResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty("bot.enabled")
public class GetClientScheduleHandlerClientRevenue extends ClientRevenueAbstractHandler {

    public GetClientScheduleHandlerClientRevenue(ObjectMapper objectMapper, ClientRevenueAnalyzerIntegrationClient client) {
        super(objectMapper, client);
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();

        if (getCommand().equals(messageText)) {
            return getPlainSendMessage(
                    chatId,
                    "Введите имя клиента, дату начала и дату окончания в формате: Имя YYYY-MM-DD YYYY-MM-DD"
            );
        }

        return sendClientSchedule(chatId, messageText);
    }

    @Override
    public String getCommand() {
        return "/clientschedule";
    }

    private BotApiMethod<?> sendClientSchedule(long chatId, String messageText) {
        String[] parts = messageText.split("\\s+");
        String clientName = parts[0];
        String startDate = parts[1];
        String endDate = parts[2];

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate leftDate = LocalDate.parse(startDate, dateFormatter);
        LocalDate rightDate = LocalDate.parse(endDate, dateFormatter);

        List<GetClientScheduleResponse> schedule = client.getClientSchedule(clientName, leftDate.atStartOfDay(), rightDate.atStartOfDay());

        return getPlainSendMessage(chatId, formatClientSchedule(schedule));
    }

    private String formatClientSchedule(List<GetClientScheduleResponse> response) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("ru"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru"));

        if (response.isEmpty()) {
            return "📅 Расписание отсутствует";
        }

        return response.stream()
                .map(schedule -> {
                    String startDate = schedule.getEventStart().format(dateFormatter);
                    String startTime = schedule.getEventStart().format(timeFormatter);
                    String endTime = schedule.getEventEnd().format(timeFormatter);

                    return String.format(
                            "📅 Дата: %s\n⏰ Время: %s - %s\n",
                            startDate, startTime, endTime
                    );
                })
                .collect(Collectors.joining("\n"));
    }

    @Override
    public boolean isApplicable(Update update) {
        boolean isMessage = update.getMessage() != null && !getCommand().equals(update.getMessage().getText());
        return super.isApplicable(update) || isMessage;
    }
}

