package ru.nesterov.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
public class PriceChangeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "CLIENT_ID", nullable = false)
    private Long clientId;

    @Column(name = "PREVIOUS_PRICE")
    private Integer previousPrice;

    @Column(name = "NEW_PRICE")
    private Integer newPrice;

    @Column(name = "CHANGE_DATE", nullable = false)
    @CreationTimestamp
    private LocalDateTime changeDate;
}
