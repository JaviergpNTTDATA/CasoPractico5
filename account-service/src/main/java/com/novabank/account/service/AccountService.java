package com.novabank.account.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.novabank.account.dto.AccountBalanceDTO;
import com.novabank.account.dto.AccountDTO;
import com.novabank.account.dto.ClientDTO;
import com.novabank.account.dto.MovementDTO;
import com.novabank.account.exception.AccountNotFoundException;
import com.novabank.account.exception.ClientNotFoundException;
import com.novabank.account.exception.InsufficientBalanceException;
import com.novabank.account.mapper.AccountMapper;
import com.novabank.account.mapper.MovementMapper;
import com.novabank.account.model.Account;
import com.novabank.account.model.Movement;
import com.novabank.account.model.MovementType;
import com.novabank.account.repository.AccountRepository;
import com.novabank.account.repository.IbanGenerator;
import com.novabank.account.repository.MovementRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final ClientIntegrationService clientIntegrationService;
    private final IbanGenerator ibanGenerator;
    private final MovementRepository movementRepository;

    public AccountService(AccountRepository accountRepository,
            ClientIntegrationService clientIntegrationService,
            IbanGenerator ibanGenerator,
            MovementRepository movementRepository) {
        this.accountRepository = accountRepository;
        this.clientIntegrationService = clientIntegrationService;
        this.ibanGenerator = ibanGenerator;
        this.movementRepository = movementRepository;
    }

    public Mono<AccountDTO> createAccount(Long clientId) {
        return clientIntegrationService.getClient(clientId)
                .flatMap(client -> {
                    if ("No disponible".equals(client.getFirstName())) {
                        return Mono.error(new ClientNotFoundException(
                                "Client service not available or client not found: " + clientId));
                    }

                    return ibanGenerator.generateIban()
                            .flatMap(iban -> {
                                Account account = new Account();
                                account.setClientId(clientId);
                                account.setIban(iban);
                                account.ensureDefaults();

                                return accountRepository.save(account)
                                        .map(AccountMapper::toDTO);
                            });
                });
    }

    public Flux<AccountDTO> listClientAccounts(Long clientId) {
        Mono<ClientDTO> clientMono = clientIntegrationService.getClient(clientId)
                .flatMap(client -> "No disponible".equals(client.getFirstName())
                        ? Mono.error(new ClientNotFoundException(
                                "Client service not available or client not found: " + clientId))
                        : Mono.just(client));

        return clientMono.thenMany(accountRepository.findByClientId(clientId)
                .switchIfEmpty(Mono.error(new AccountNotFoundException(
                        "No accounts found for client with ID: " + clientId)))
                .map(AccountMapper::toDTO));
    }

    public Mono<AccountDTO> getAccountByIban(String iban) {
        return accountRepository.findByIban(iban)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with IBAN: " + iban)))
                .map(AccountMapper::toDTO);
    }

    public Mono<MovementDTO> deposit(String iban, BigDecimal amount) {
        validateAmount(amount);

        return accountRepository.findByIban(iban)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with IBAN: " + iban)))
                .flatMap(account -> {
                    account.ensureDefaults();
                    account.setBalance(account.getBalance().add(amount));

                    Movement movement = Movement.builder()
                            .accountId(account.getId())
                            .type(MovementType.DEPOSIT)
                            .amount(amount)
                            .createdAt(LocalDateTime.now())
                            .build();
                    movement.ensureDefaults();

                    return accountRepository.save(account)
                            .then(movementRepository.save(movement))
                            .map(saved -> MovementMapper.toDto(saved, account));
                });
    }

    public Mono<MovementDTO> withdraw(String iban, BigDecimal amount) {
        validateAmount(amount);

        return accountRepository.findByIban(iban)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with IBAN: " + iban)))
                .flatMap(account -> {
                    account.ensureDefaults();
                    if (account.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new InsufficientBalanceException("Insufficient balance"));
                    }

                    account.setBalance(account.getBalance().subtract(amount));

                    Movement movement = Movement.builder()
                            .accountId(account.getId())
                            .type(MovementType.WITHDRAW)
                            .amount(amount)
                            .createdAt(LocalDateTime.now())
                            .build();
                    movement.ensureDefaults();

                    return accountRepository.save(account)
                            .then(movementRepository.save(movement))
                            .map(saved -> MovementMapper.toDto(saved, account));
                });
    }

    public Mono<MovementDTO> transferWithdraw(String iban, BigDecimal amount) {
        validateAmount(amount);

        return accountRepository.findByIban(iban)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with IBAN: " + iban)))
                .flatMap(account -> {
                    account.ensureDefaults();
                    if (account.getBalance().compareTo(amount) < 0) {
                        return Mono.error(new InsufficientBalanceException("Insufficient balance"));
                    }

                    account.setBalance(account.getBalance().subtract(amount));

                    Movement movement = Movement.builder()
                            .accountId(account.getId())
                            .type(MovementType.OUTGOING_TRANSFER)
                            .amount(amount)
                            .createdAt(LocalDateTime.now())
                            .build();
                    movement.ensureDefaults();

                    return accountRepository.save(account)
                            .then(movementRepository.save(movement))
                            .map(saved -> MovementMapper.toDto(saved, account));
                });
    }

    public Mono<MovementDTO> transferDeposit(String iban, BigDecimal amount) {
        validateAmount(amount);

        return accountRepository.findByIban(iban)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with IBAN: " + iban)))
                .flatMap(account -> {
                    account.ensureDefaults();
                    account.setBalance(account.getBalance().add(amount));

                    Movement movement = Movement.builder()
                            .accountId(account.getId())
                            .type(MovementType.INCOMING_TRANSFER)
                            .amount(amount)
                            .createdAt(LocalDateTime.now())
                            .build();
                    movement.ensureDefaults();

                    return accountRepository.save(account)
                            .then(movementRepository.save(movement))
                            .map(saved -> MovementMapper.toDto(saved, account));
                });
    }

    public Mono<AccountBalanceDTO> getBalanceByIban(String iban) {
        return accountRepository.findByIban(iban)
                .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found with IBAN: " + iban)))
                .map(account -> new AccountBalanceDTO(account.getIban(), account.getBalance()));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
