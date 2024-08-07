package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.Client;


@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findClientByName(String name);

    @Query(value = "FROM Client c where c.name like :name")
    Client findClientLikeName(String name);
}
