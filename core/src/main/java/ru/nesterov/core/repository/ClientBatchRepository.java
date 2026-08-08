package ru.nesterov.core.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.nesterov.core.service.dto.ClientDto;
import ru.nesterov.core.service.dto.UserDto;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClientBatchRepository {
    private final JdbcTemplate jdbcTemplate;

    public List<ClientDto> createClient(UserDto userDto, List<ClientDto> clientDtoList) {
        String createClientSql = "INSERT INTO client (name, description, active, user_id, start_date, phone) values(?, ?, ?, ?, ?, ?) RETURNING id";
        String priceChangeHistorySql = "INSERT INTO price_change_history (client_id, price, change_date) values(?, ?, ?)";

        List<Long> IdList = new ArrayList<>();

        jdbcTemplate.execute((Connection connection) -> {
            PreparedStatement ps = connection.prepareStatement(createClientSql);
            for (ClientDto dto : clientDtoList) {
                ps.setString(1, dto.getName());
                ps.setString(2, dto.getDescription());
                ps.setBoolean(3, dto.isActive());
                ps.setLong(4, userDto.getId());
                ps.setDate(5, new java.sql.Date(dto.getStartDate().getTime()));
                ps.setString(6, dto.getPhone());
                ps.addBatch();
            }
            ps.executeBatch();

            try (ResultSet rs = ps.getResultSet()) {
                while (rs.next()) {
                    IdList.add(rs.getLong(1));
                }
            }
            ps.close();
            return null;
        });

        for (int i = 0; i < clientDtoList.size(); i++) {
            clientDtoList.get(i).setId(IdList.get(i));
        }

        jdbcTemplate.batchUpdate(priceChangeHistorySql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ClientDto clientDto = clientDtoList.get(i);
                ps.setLong(1, clientDto.getId());
                ps.setInt(2, clientDto.getPricePerHour());
                ps.setDate(3, (Date) clientDto.getStartDate());
            }

            @Override
            public int getBatchSize() {
                return clientDtoList.size();
            }
        });

        return clientDtoList;
    }

    public void deleteClient() {
        // TODO DELETE
    }
}
