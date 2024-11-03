package ru.nesterov.service.date.helper;

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
            switch (month) {
                case JANUARY -> MONTH_MAP.put("январь", month);
                case FEBRUARY -> MONTH_MAP.put("февраль", month);
                case MARCH -> MONTH_MAP.put("март", month);
                case APRIL -> MONTH_MAP.put("апрель", month);
                case MAY -> MONTH_MAP.put("май", month);
                case JUNE -> MONTH_MAP.put("июнь", month);
                case JULY -> MONTH_MAP.put("июль", month);
                case AUGUST -> MONTH_MAP.put("август", month);
                case SEPTEMBER -> MONTH_MAP.put("сентябрь", month);
                case OCTOBER -> MONTH_MAP.put("октябрь", month);
                case NOVEMBER -> MONTH_MAP.put("ноябрь", month);
                case DECEMBER -> MONTH_MAP.put("декабрь", month);
            }
        }
    }

    public static String getMonthNameByNumber(int monthNumber) {
        Month month = Month.of(monthNumber);
        return switch (month) {
            case JANUARY -> "Январь";
            case FEBRUARY -> "Февраль";
            case MARCH -> "Март";
            case APRIL -> "Апрель";
            case MAY -> "Май";
            case JUNE -> "Июнь";
            case JULY -> "Июль";
            case AUGUST -> "Август";
            case SEPTEMBER -> "Сентябрь";
            case OCTOBER -> "Октябрь";
            case NOVEMBER -> "Ноябрь";
            case DECEMBER -> "Декабрь";
        };
    }


    public static MonthDatesPair getFirstAndLastDayOfMonth(String monthName) {
        monthName = monthName.toLowerCase();
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
