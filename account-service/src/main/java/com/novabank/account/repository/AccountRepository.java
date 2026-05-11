package com.novabank.account.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.novabank.account.model.Account;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<Account, Long> {
    Mono<Account> findByIban(String iban);

    Flux<Account> findByClientId(Long clientId);
}
