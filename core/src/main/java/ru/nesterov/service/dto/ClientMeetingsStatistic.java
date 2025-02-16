package ru.nesterov.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientMeetingsStatistic {
    private double successfulMeetingsHours;
    private double cancelledMeetingsHours;
    private double incomePerHour;
    private int successfulEventsCount;
    private int plannedCancelledEventsCount;
    private int notPlannedCancelledEventsCount;

    public ClientMeetingsStatistic(double incomePerHour) {
        this.incomePerHour = incomePerHour;
    }

    public void increaseSuccessfulHours(double hours) {
        successfulMeetingsHours += hours;
    }

    public void increaseCancelled(double hours) {
        cancelledMeetingsHours += hours;
    }

    public double getSuccessfulMeetingsPercentage() {
        return 100.0 * successfulMeetingsHours / (successfulMeetingsHours + cancelledMeetingsHours);
    }

    public double getLostIncome() {
        return cancelledMeetingsHours * incomePerHour;
    }

    public double getActualIncome() {
        return successfulMeetingsHours * incomePerHour;
    }

    public void increaseSuccessfulEvents(int events) {
        successfulEventsCount += events;
    }

    public void increasePlannedCancelledEvents(int events) {
        plannedCancelledEventsCount += events;
    }

    public void increaseNotPlannedCancelledEvents(int events) {
        notPlannedCancelledEventsCount += events;
    }

    public boolean isFilledStatistic() {
        return successfulMeetingsHours != 0.0
                || cancelledMeetingsHours != 0.0
                || incomePerHour != 0.0
                || successfulEventsCount != 0
                || plannedCancelledEventsCount != 0
                || notPlannedCancelledEventsCount != 0;
    }

    @Override
    public String toString() {
        return "ClientMeetingsStatistic{" +
                "successfulMeetingsHours=" + successfulMeetingsHours +
                ", cancelledMeetingsHours=" + cancelledMeetingsHours +
                ", successfulMeetingsPercentage=" + getSuccessfulMeetingsPercentage() +
                ", lostIncome=" + getLostIncome() +
                ", actualIncome=" + getActualIncome() +
                ", incomePerHour=" + incomePerHour +
                ", successfulEvents=" + successfulEventsCount +
                ", plannedCancelledEvents=" + plannedCancelledEventsCount +
                ", notPlannedCancelledEvents=" + notPlannedCancelledEventsCount +
                '}';
    }
}
