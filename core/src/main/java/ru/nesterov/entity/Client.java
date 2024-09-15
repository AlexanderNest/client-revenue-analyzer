package ru.nesterov.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Data
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_seq_gen")
    @SequenceGenerator(name = "client_seq_gen", sequenceName = "client_seq", allocationSize = 1)
    private long id;

    private String name;
    private int pricePerHour;
    private String description;
    private boolean active;
    private Date startDate;
    private String phone;
}
