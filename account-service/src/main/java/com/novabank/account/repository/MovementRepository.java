package com.novabank.account.repository;

import java.time.LocalDateTime;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.novabank.account.model.Movement;

import reactor.core.publisher.Flux;

@Repository
public interface MovementRepository extends ReactiveCrudRepository<Movement, Long> {

    Flux<Movement> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    Flux<Movement> findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long accountId, LocalDateTime start,
            LocalDateTime end);

}
