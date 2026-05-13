package com.novabank.client.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.novabank.client.model.Client;

import reactor.core.publisher.Mono;

@Repository
public interface ClientRepository extends ReactiveCrudRepository<Client, Long> {

    Mono<Client> findByDni(String dni);

    Mono<Client> findByEmail(String email);

    Mono<Boolean> existsByDni(String dni);

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByPhone(String phone);
}
