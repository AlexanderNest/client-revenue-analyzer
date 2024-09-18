package ru.nesterov.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String username;
    @Column(name = "main_calendar", nullable = false)
    private String mainCalendar;
    @Column(name = "cancelled_calender")
    private String cancelledCalendar;

    @OneToMany(mappedBy = "user")
    private List<Client> clients;
}
