package ru.nesterov.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nesterov.core.entity.Client;

import java.util.List;


@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findClientByNameAndUserId(String name, long userId);

    @Query(value = "SELECT * FROM client WHERE (name = :name OR name REGEXP '^' || :name || '\\s\\d+$') and user_id = :userId", nativeQuery = true)
    List<Client> findAllByExactNameOrNameStartingWithAndEndingWithNumberAndUserId(@Param("name") String name, @Param("userId") long userId);

    @Query(value = """
            SELECT c.id, c.name, c.description, c.active, c.user_id, c.start_date, c.phone FROM client c
            JOIN price_change_history pch ON c.id = pch.client_id
            WHERE c.user_id = :userId
            AND c.active = :active
            AND pch.change_date = (SELECT MAX(change_date) FROM price_change_history WHERE client_id = c.id)
            ORDER BY pch.price DESC
           """, nativeQuery = true)
    List<Client> findClientByUserIdAndActiveOrderByPricePerHourDesc(@Param("userId") long userId, @Param("active") boolean active);

    int deleteClientByNameAndUserId(String name, long userId);
}
