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
            SELECT id, name, description, price_per_hour, active, user_id, start_date, phone
            FROM client 
            WHERE (name = :name
                OR REGEXP_LIKE(name, CONCAT('^', :name, ' [0-9]+$'))) 
            AND user_id = :userId
            """, nativeQuery = true)
    List<Client> findAllByExactNameOrNameStartingWithAndEndingWithNumberAndUserId(@Param("name") String name, @Param("userId") long userId);

    List<Client> findClientByUserIdAndActiveOrderByPricePerHourDesc(long userId, boolean active);

    int deleteClientByNameAndUserId(String name, long userId);
}
