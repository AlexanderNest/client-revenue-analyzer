package ru.nesterov.core.service.date.helper;

public class WeekHelper {
    public static String getWeekDayNameByNumber(int weekDayNumber){

        return switch (weekDayNumber) {
            case 1 -> "Понедельник";
            case 2 -> "Вторник";
            case 3 -> "Среда";
            case 4 -> "Четверг";
            case 5 -> "Пятница";
            case 6 -> "Суббота";
            case 7 -> "Воскресенье";
            default -> throw  new IllegalArgumentException("Дня недели с номером" + weekDayNumber + " не существует");
        };
    }
}
