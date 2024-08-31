package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.Client;

import java.util.List;


@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findClientByNameAndUserId(String name, long userId);

    List<Client> findAllByNameContainingAndUserId(String name, long userId);

    List<Client> findClientByUserIdAndActiveOrderByPricePerHourDesc(long userId, boolean active);

    int deleteClientByNameAndUserId(String name, long userId);
}
