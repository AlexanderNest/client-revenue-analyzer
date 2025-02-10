package ru.nesterov.formatter;

import ru.nesterov.service.dto.ClientMeetingsStatistic;

import java.util.Map;
import java.util.Set;

public class ClientAnalyticsFormatter {
    public static String format(Set<Map.Entry<String, ClientMeetingsStatistic>> statistics) {
        StringBuilder result = new StringBuilder();
        if (statistics != null) {
            statistics.forEach(entry -> {
                String clientName = entry.getKey();
                ClientMeetingsStatistic stat = entry.getValue();
                result.append(clientName)
                        .append(":")
                        .append("SuccessfulHours|").append(stat.getSuccessfulMeetingsHours())
                        .append(",")
                        .append("CancelledHours|").append(stat.getCancelledMeetingsHours())
                        .append(",")
                        .append("SuccessRate|")
                        .append(String.format("%.2f", stat.getSuccessfulMeetingsPercentage()))
                        .append("%,")
                        .append("LostIncome|")
                        .append(String.format("%.2f", stat.getLostIncome()))
                        .append(",")
                        .append("ActualIncome|")
                        .append(String.format("%.2f", stat.getActualIncome()))
                        .append(",")
                        .append("SuccessfulEvents|")
                        .append(stat.getSuccessfulEventsCount())
                        .append(",")
                        .append("PlannedCancelledEvents|")
                        .append(stat.getPlannedCancelledEventsCount())
                        .append(",")
                        .append("NotPlannedCancelledEvents|")
                        .append(stat.getNotPlannedCancelledEventsCount())
                        .append(";");
            });
        }
        return result.toString().trim();
    }
}
