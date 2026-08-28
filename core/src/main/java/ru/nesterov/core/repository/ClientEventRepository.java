package ru.nesterov.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nesterov.core.entity.ClientEvent;

import java.util.List;

@Repository
public interface ClientEventRepository extends JpaRepository<ClientEvent, Long> {
    @Query("SELECT ce.eventId FROM ClientEvent ce WHERE ce.clientId = :clientId")
    List<String> getEventIdsByClientId(Long clientId);
}
