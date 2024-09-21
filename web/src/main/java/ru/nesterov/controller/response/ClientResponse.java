package ru.nesterov.controller.response;

import lombok.Builder;
import lombok.Data;
import org.glassfish.grizzly.http.util.TimeStamp;

import java.util.Date;

@Data
@Builder
public class ClientResponse {
    private long id;
    private String name;
    private int pricePerHour;
    private String description;
    private boolean active;
    private Date startDate;
    private String phone;
}
