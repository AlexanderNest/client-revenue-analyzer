package ru.nesterov.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "client_events")
public class ClientEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_events_seq")
    @SequenceGenerator(name = "client_events_seq", sequenceName = "client_events_seq", allocationSize = 1)
    private Long id;
    @Column(name = "client_id")
    private Long clientId;
    @Column(name = "event_id")
    private String eventId;
}
