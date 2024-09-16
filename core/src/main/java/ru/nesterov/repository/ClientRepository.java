package ru.nesterov.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.Client;

import java.util.List;


@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findClientByName(String name);

    @Query(value = "SELECT * FROM client WHERE name = :name OR name REGEXP '^' || :name || '\\s\\d+$'", nativeQuery = true)
    List<Client> findAllByExactNameOrNameStartingWithAndEndingWithNumber(@Param("name") String name);

    List<Client> findClientByActiveOrderByPricePerHourDesc(boolean active);

    int deleteClientByName(String name);
}
