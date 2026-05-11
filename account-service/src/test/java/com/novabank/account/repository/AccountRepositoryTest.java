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
@org.junit.jupiter.api.Disabled("Pendiente de configurar BD de test R2DBC (Testcontainers o properties). Este test requiere PostgreSQL/ConnectionFactory.")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void saveAndFindByClientId_shouldWork() {
        Account acc = new Account();
        acc.setClientId(1L);
        acc.setIban("ES123");
        acc.setBalance(BigDecimal.ZERO);
        acc.ensureDefaults();

        Mono<Account> firstFound = accountRepository.save(acc)
                .thenMany(accountRepository.findByClientId(1L))
                .next();

        StepVerifier.create(firstFound)
                .assertNext(found -> {
                    assertEquals("ES123", found.getIban());
                    assertEquals(1L, found.getClientId());
                })
                .verifyComplete();
    }

    @Test
    void saveAndFindByIban_shouldWork() {
        Account acc = new Account();
        acc.setClientId(2L);
        acc.setIban("ES999");
        acc.setBalance(BigDecimal.TEN);
        acc.ensureDefaults();

        Mono<Account> foundMono = accountRepository.save(acc)
                .then(accountRepository.findByIban("ES999"));

        StepVerifier.create(foundMono)
                .assertNext(found -> {
                    assertEquals(2L, found.getClientId());
                    assertEquals("ES999", found.getIban());
                })
                .verifyComplete();
    }
}
