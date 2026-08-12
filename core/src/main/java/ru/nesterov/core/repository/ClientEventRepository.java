package ru.nesterov.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nesterov.core.entity.ClientEvent;

@Repository
public interface ClientEventRepository extends JpaRepository<ClientEvent, Long> {

}
