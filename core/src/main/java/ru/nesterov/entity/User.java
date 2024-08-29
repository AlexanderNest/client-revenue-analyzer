package ru.nesterov.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "\"user\"")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;
    @Column(name = "main_calendar", nullable = false)
    private String mainCalendar;
    @Column(name = "cancelled_calender")
    private String cancelledCalendar;
}
