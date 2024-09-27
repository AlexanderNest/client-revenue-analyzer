package ru.nesterov;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public enum DayOfWeekRu {
    ПН(DayOfWeek.MONDAY),
    ВТ(DayOfWeek.TUESDAY),
    СР(DayOfWeek.WEDNESDAY),
    ЧТ(DayOfWeek.THURSDAY),
    ПТ(DayOfWeek.FRIDAY),
    СБ(DayOfWeek.SATURDAY),
    ВС(DayOfWeek.SUNDAY);

    DayOfWeekRu(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    private static final Map<DayOfWeek, DayOfWeekRu> dayOfWeekMap = new HashMap<>();
    private final DayOfWeek dayOfWeek;

    static {
        for (DayOfWeekRu dayRu : DayOfWeekRu.values()) {
            dayOfWeekMap.put(dayRu.getDayOfWeek(), dayRu);
        }
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public static DayOfWeekRu from(DayOfWeek dayOfWeek) {
        return dayOfWeekMap.get(dayOfWeek);
    }
}
