package com.novabank.account.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

@Component
public class IbanGenerator {

    private final DatabaseClient databaseClient;

    public IbanGenerator(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    /**
     * Generates an IBAN using a PostgreSQL sequence (iban_seq).
     * Requires the sequence to exist: CREATE SEQUENCE iban_seq;
     */
    public Mono<String> generateIban() {
        return databaseClient.sql("SELECT nextval('iban_seq') AS seq")
                .map(row -> row.get("seq", Long.class))
                .one()
                .map(seq -> "ES91210000" + String.format("%012d", seq));
    }
}
