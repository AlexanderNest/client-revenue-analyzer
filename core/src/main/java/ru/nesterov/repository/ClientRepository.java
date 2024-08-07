package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.Client;


@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findClientByName(String name);
}
