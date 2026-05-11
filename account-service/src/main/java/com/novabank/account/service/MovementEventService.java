package com.novabank.account.service;

import org.springframework.stereotype.Service;

import com.novabank.account.dto.MovementDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class MovementEventService {

    private final Sinks.Many<MovementDTO> sink = Sinks.many()
            .multicast()
            .onBackpressureBuffer();

    public void publish(MovementDTO movement) {
        sink.tryEmitNext(movement);
    }

    /**
     * En este proyecto MovementDTO no expone accountId, solo IBAN.
     * Para poder filtrar de forma consistente, el stream se publica por IBAN.
     */
    public Flux<MovementDTO> streamByIban(String iban) {
        String normalized = iban == null ? null : iban.toUpperCase();

        return sink.asFlux()
                .filter(m -> m != null && m.iban() != null
                        && normalized != null
                        && m.iban().equalsIgnoreCase(normalized));
    }
}
