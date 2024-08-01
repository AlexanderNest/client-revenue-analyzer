package ru.nesterov.clientRevenueAnalyzer.service.monthHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

public class MonthHelper {
    private static final Map<String, Month> MONTH_MAP = new HashMap<>();

    static {
        for (Month month : Month.values()) {
            MONTH_MAP.put(month.name().toLowerCase(), month);
        }
    }

    public static MonthDatesPair getFirstAndLastDayOfMonth(String monthName) {
        Month month = MONTH_MAP.get(monthName.toLowerCase());
        if (month == null) {
            throw new IllegalArgumentException("Invalid month name: " + monthName);
        }

        int year = LocalDate.now().getYear();
        YearMonth yearMonth = YearMonth.of(year, month);

        LocalDateTime firstDay = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime lastDay = yearMonth.atEndOfMonth().atTime(23, 59, 59, 999999999);


        return new MonthDatesPair(firstDay, lastDay);
    }
}
