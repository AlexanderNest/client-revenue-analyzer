package ru.nesterov.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.nesterov.core.entity.PriceChangeHistory;
import java.util.List;

@Repository
public interface PriceChangeHistoryRepository extends JpaRepository<PriceChangeHistory, Long> {
    List<PriceChangeHistory> findByClientId(Long id);

    @Transactional
    @Modifying
    @Query("delete from PriceChangeHistory p where p.clientId = :clientId")
    void deleteByClientId(@Param("clientId") Long clientId);
}
