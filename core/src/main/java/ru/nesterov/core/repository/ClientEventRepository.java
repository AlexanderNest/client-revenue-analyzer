package ru.nesterov.core.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.nesterov.calendar.integration.dto.ClientEventDto;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClientEventRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<ClientEventDto> batchCreateRelation(List<ClientEventDto> clientEventDtoList) {
        String createRelationSql = "INSERT INTO client_events (client_id, event_id)";

        jdbcTemplate.batchUpdate(createRelationSql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ClientEventDto clientEventDto = clientEventDtoList.get(i);
                ps.setLong(1, clientEventDto.getClientId());
                ps.setString(2, clientEventDto.getEventId());
            }

            @Override
            public int getBatchSize() {
                return clientEventDtoList.size();
            }
        });

        return clientEventDtoList;
    }

    public void deleteRelation() {

    }
}
