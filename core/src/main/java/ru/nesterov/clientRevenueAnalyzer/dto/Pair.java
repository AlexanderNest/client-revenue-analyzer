package ru.nesterov.clientRevenueAnalyzer.dto;

import lombok.Value;

@Value
public class Pair <L, R> {
    private final L first;
    private final R second;
}
