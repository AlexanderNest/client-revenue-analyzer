package ru.nesterov.clientRevenueAnalyzer.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientMeetingsStatistic {
    private double successfulMeetingsHours;
    private double cancelledMeetingsHours;
    private double successfulMeetingsPercentage;
    private double incomePerHour;

    public ClientMeetingsStatistic(double incomePerHour) {
        this.incomePerHour = incomePerHour;
    }

    public void increaseSuccessful(double hours) {
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

    @Override
    public String toString() {
        return "ClientMeetingsStatistic{" +
                "successfulMeetingsHours=" + successfulMeetingsHours +
                ", cancelledMeetingsHours=" + cancelledMeetingsHours +
                ", successfulMeetingsPercentage=" + getSuccessfulMeetingsPercentage() +
                ", lostIncome=" + getLostIncome() +
                ", actualIncome=" + getActualIncome() +
                ", incomePerHour=" + incomePerHour +
                '}';
    }
}
