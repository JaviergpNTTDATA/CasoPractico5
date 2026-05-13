package com.novabank.account.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;

import com.novabank.account.model.Account;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DataR2dbcTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void saveAndFindByClientId_shouldWork() {
        // Unique IBAN per test run to avoid collisions with preloaded data / previous runs.
        // Column iban is limited (varchar(34)), so we avoid going overboard with UUID length.
        String iban = ("TEST" + java.util.UUID.randomUUID().toString().replace("-", "")).substring(0, 34);

        Account acc = new Account();
        acc.setClientId(1L);
        acc.setIban(iban);
        acc.setBalance(BigDecimal.ZERO);
        acc.ensureDefaults();

        Mono<Account> firstFound = accountRepository.save(acc)
                .thenMany(accountRepository.findByClientId(1L))
                .filter(a -> iban.equals(a.getIban()))
                .next();

        StepVerifier.create(firstFound)
                .assertNext(found -> {
                    assertEquals(iban, found.getIban());
                    assertEquals(1L, found.getClientId());
                })
                .verifyComplete();
    }

    @Test
    void saveAndFindByIban_shouldWork() {
        // Unique IBAN per test run to avoid collisions with preloaded data / previous runs.
        // Column iban is limited (varchar(34)), so we avoid going overboard with UUID length.
        String iban = ("TEST" + java.util.UUID.randomUUID().toString().replace("-", "")).substring(0, 34);

        Account acc = new Account();
        acc.setClientId(2L);
        acc.setIban(iban);
        acc.setBalance(BigDecimal.TEN);
        acc.ensureDefaults();

        Mono<Account> foundMono = accountRepository.save(acc)
                .then(accountRepository.findByIban(iban));

        StepVerifier.create(foundMono)
                .assertNext(found -> {
                    assertEquals(2L, found.getClientId());
                    assertEquals(iban, found.getIban());
                })
                .verifyComplete();
    }
}
