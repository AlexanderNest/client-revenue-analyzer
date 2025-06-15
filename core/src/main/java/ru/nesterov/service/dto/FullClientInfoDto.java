package ru.nesterov.service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class FullClientInfoDto {
    private long id;
    private String name;
    private int pricePerHour;
    private String description;
    private Date startDate;
    private int serviceDuration;
    private String phone;
    private int totalMeetings;
    private int totalMeetingsHours;
    private int totalIncome;
    private int unplannedCancelledEventsCount;
    private int plannedCancelledEventsCount;
}
