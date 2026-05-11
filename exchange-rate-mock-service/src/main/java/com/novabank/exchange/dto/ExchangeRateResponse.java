package com.novabank.exchange.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ExchangeRateResponse(
        String from,
        String to,
        BigDecimal rate,
        Instant timestamp) {
}
