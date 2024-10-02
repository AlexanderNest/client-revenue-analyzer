package ru.nesterov.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.callback.ButtonCallback;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class InlineCalendarBuilder {
    private final ObjectMapper objectMapper;

    public InlineKeyboardMarkup createCalendarMarkup(LocalDate dateForDisplay, String command) {
        YearMonth yearMonth = YearMonth.of(dateForDisplay.getYear(), dateForDisplay.getMonth());
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(getHeaderRow(
                dateForDisplay.getMonth().toString() + " " + dateForDisplay.getYear(),
                command));
        rowsInline.add(getDaysOfWeek());
        rowsInline.addAll(getDaysOfMonthRows(firstDayOfMonth, lastDayOfMonth, command));

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    @SneakyThrows
    private List<InlineKeyboardButton> getHeaderRow(String text, String command) {
        List<InlineKeyboardButton> headerRow = new ArrayList<>();
        ButtonCallback prevCallback = new ButtonCallback();
        prevCallback.setCommand(command);
        prevCallback.setValue("Prev");

        ButtonCallback nextCallback = new ButtonCallback();
        nextCallback.setCommand(command);
        nextCallback.setValue("Next");

        headerRow.add(InlineKeyboardButton.builder()
                .text("◀")
                .callbackData(objectMapper.writeValueAsString(prevCallback))
                .build());
        headerRow.add(InlineKeyboardButton.builder()
                .text(text)
                .callbackData("ignore")
                .build());
        headerRow.add(InlineKeyboardButton.builder()
                .text("▶")
                .callbackData(objectMapper.writeValueAsString(nextCallback))
                .build());

        return headerRow;
    }

    private List<InlineKeyboardButton> getDaysOfWeek() {
        List<InlineKeyboardButton> daysOfWeekRow = new ArrayList<>();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            daysOfWeekRow.add(InlineKeyboardButton.builder()
                    .text(dayOfWeek.name().substring(0, 2))
                    .callbackData("ignore")
                    .build());
        }
        return daysOfWeekRow;
    }

    @SneakyThrows
    private List<List<InlineKeyboardButton>> getDaysOfMonthRows(LocalDate firstDayOfMonth,
                                                                       LocalDate lastDayOfMonth,
                                                                       String command) {
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        for (int i = 1; i < firstDayOfMonth.getDayOfWeek().getValue(); i++) {
            rowInline.add(InlineKeyboardButton.builder()
                    .text(" ")
                    .callbackData("ignore")
                    .build());
        }

        for (LocalDate day = firstDayOfMonth; !day.isAfter(lastDayOfMonth); day = day.plusDays(1)) {
            ButtonCallback dayCallback = new ButtonCallback();
            dayCallback.setCommand(command);
            dayCallback.setValue(day.toString());

            rowInline.add(InlineKeyboardButton.builder()
                    .text(String.valueOf(day.getDayOfMonth()))
                    .callbackData(objectMapper.writeValueAsString(dayCallback))
                    .build());

            if (rowInline.size() == 7) {
                rowsInline.add(rowInline);
                rowInline = new ArrayList<>();
            }
        }

        if (!rowInline.isEmpty()) {
            while (rowInline.size() < 7) {
                rowInline.add(InlineKeyboardButton.builder()
                        .text(" ")
                        .callbackData("ignore")
                        .build());
            }
            rowsInline.add(rowInline);
        }

        return rowsInline;
    }
}