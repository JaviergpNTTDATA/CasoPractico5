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
     * Genera IBAN usando una secuencia PostgreSQL (iban_seq).
     * Requiere que exista: CREATE SEQUENCE iban_seq;
     */
    public Mono<String> generateIban() {
        return databaseClient.sql("SELECT nextval('iban_seq') AS seq")
                .map(row -> row.get("seq", Long.class))
                .one()
                .map(seq -> "ES91210000" + String.format("%012d", seq));
    }
}
