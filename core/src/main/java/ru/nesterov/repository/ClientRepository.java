package ru.nesterov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nesterov.entity.Client;

import java.util.List;


@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findClientByNameAndUserId(String name, long userId);

    @Query(value = "SELECT * FROM client WHERE (name = :name OR name REGEXP '^' || :name || '\\s\\d+$') and user_id = :userId", nativeQuery = true)
    List<Client> findAllByExactNameOrNameStartingWithAndEndingWithNumberAndUserId(@Param("name") String name, @Param("userId") long userId);

    List<Client> findClientByUserIdAndActiveOrderByPricePerHourDesc(long userId, boolean active);

    int deleteClientByNameAndUserId(String name, long userId);
}
