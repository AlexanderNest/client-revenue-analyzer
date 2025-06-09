package ru.nesterov.service.database;

import org.springframework.stereotype.Service;

import java.util.Map;
@Service
public class IndexesService {
    private final Map<String, String> indexes = Map.of(
            "PUBLIC.IDX_UNIQUE_PHONE_PER_USER", "Номер телефона",
            "IDX_UNIQUE_CLIENT_NAME_PER_USER", "Имя клиента"
    );

    public String getAlias(String indexName){
        return indexes.get(indexName);
    }

}
