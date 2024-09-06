package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.Client;

import java.util.List;


@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findClientByName(String name);

    List<Client> findAllByNameContaining(String name);

    List<Client> findAllByNameEquals(String name);

    List<Client> findClientByActiveOrderByPricePerHourDesc(boolean active);

    int deleteClientByName(String name);
}
