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

    @Query(value = """
            SELECT id, name, description, active, user_id, start_date, phone
            FROM client
            WHERE (name = :name
                OR name ~ CONCAT ('^', :name, ' [0-9]+$'))
            AND user_id = :userId
            """, nativeQuery = true)
    List<Client> findAllByExactNameOrNameStartingWithAndEndingWithNumberAndUserId(@Param("name") String name, @Param("userId") long userId);

    @Query(value = """
            SELECT c FROM Client c
            JOIN c.priceChangeHistory pch
            WHERE c.user.id = :userId
            AND c.active = :active
            AND pch.changeDate = (
                      SELECT MAX(p.changeDate)
                      FROM PriceChangeHistory p
                      WHERE p.client.id = c.id)
            ORDER BY pch.price DESC
            """)
    List<Client> findClientByUserIdAndActiveOrderByPricePerHourDesc(@Param("userId") long userId, @Param("active") boolean active);

    int deleteClientByNameAndUserId(String name, long userId);
}
