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
     * In this service, we only make use of MovementDTO
     * To be able to filter consistently, the stream is published by IBAN.
     */
    public Flux<MovementDTO> streamByIban(String iban) {
        String normalized = iban == null ? null : iban.toUpperCase();

        return sink.asFlux()
                .filter(m -> m != null && m.iban() != null
                        && normalized != null
                        && m.iban().equalsIgnoreCase(normalized));
    }
}
