package com.novabank.account.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.novabank.account.dto.MovementDTO;
import com.novabank.account.exception.AccountNotFoundException;
import com.novabank.account.exception.MovementNotFoundException;
import com.novabank.account.mapper.MovementMapper;
import com.novabank.account.model.Account;
import com.novabank.account.repository.AccountRepository;
import com.novabank.account.repository.MovementRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class InquiryService {

        private final MovementRepository movementRepository;
        private final AccountRepository accountRepository;

        public InquiryService(MovementRepository movementRepository, AccountRepository accountRepository) {
                this.movementRepository = movementRepository;
                this.accountRepository = accountRepository;
        }

        public Flux<MovementDTO> getByAccount(String iban) {
                Mono<Account> accountMono = accountRepository.findByIban(iban)
                                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                                                "Account not found with IBAN: " + iban)));

                return accountMono.flatMapMany(account -> movementRepository.findByAccountIdOrderByCreatedAtDesc(account.getId())
                                .switchIfEmpty(Mono.error(new MovementNotFoundException(
                                                "No movements found for account: " + iban)))
                                .map(movement -> MovementMapper.toDto(movement, account)));
        }

        public Flux<MovementDTO> getByAccountAndDates(String iban, LocalDate start, LocalDate end) {
                if (start.isAfter(end)) {
                        return Flux.error(new IllegalArgumentException("Start date cannot be after end date"));
                }

                LocalDateTime startDateTime = start.atStartOfDay();
                LocalDateTime endDateTime = end.atTime(23, 59, 59);

                Mono<Account> accountMono = accountRepository.findByIban(iban)
                                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                                                "Account not found with IBAN: " + iban)));

                return accountMono.flatMapMany(account -> movementRepository
                                .findByAccountIdAndCreatedAtBetweenOrderByCreatedAtDesc(account.getId(), startDateTime, endDateTime)
                                .switchIfEmpty(Mono.error(new MovementNotFoundException(
                                                "No movements found for account: "
                                                                + iban + " between " + start + " and " + end)))
                                .map(movement -> MovementMapper.toDto(movement, account)));
        }
}
