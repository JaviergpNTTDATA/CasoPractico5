package com.novabank.operation.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.novabank.operation.client.AccountServiceClient;
import com.novabank.operation.client.ExchangeRateClient;
import com.novabank.operation.dto.AccountDTO;
import com.novabank.operation.dto.DepositWithdrawDTO;
import com.novabank.operation.dto.DepositWithdrawRequest;
import com.novabank.operation.dto.TransferDTO;
import com.novabank.operation.dto.TransferRequest;
import com.novabank.operation.exception.ExchangeRateRequiredException;
import com.novabank.operation.exception.InsufficientBalanceException;

import reactor.core.publisher.Mono;

@Service
public class OperationService {

    private final AccountServiceClient accountServiceClient;
    private final ExchangeRateClient exchangeRateClient;

    public OperationService(AccountServiceClient accountServiceClient, ExchangeRateClient exchangeRateClient) {
        this.accountServiceClient = accountServiceClient;
        this.exchangeRateClient = exchangeRateClient;
    }

    public Mono<DepositWithdrawDTO> deposit(DepositWithdrawRequest request) {
        validateAmount(request.amount());

        return accountServiceClient.getAccountByIban(request.iban())
                .then(accountServiceClient.deposit(request.iban(), request.amount()))
                .map(movement -> new DepositWithdrawDTO(
                        "DEPOSIT",
                        request.iban(),
                        request.amount(),
                        movement.timestamp()));
    }

    public Mono<DepositWithdrawDTO> withdraw(DepositWithdrawRequest request) {
        validateAmount(request.amount());

        return accountServiceClient.getAccountByIban(request.iban())
                .flatMap(account -> {
                    ensureSufficientBalance(account.balance(), request.amount());
                    return accountServiceClient.withdraw(request.iban(), request.amount())
                            .map(movement -> new DepositWithdrawDTO(
                                    "WITHDRAW",
                                    request.iban(),
                                    request.amount(),
                                    movement.timestamp()));
                });
    }

    public Mono<TransferDTO> transfer(TransferRequest request) {
        validateAmount(request.amount());

        if (request.sourceIban().equalsIgnoreCase(request.targetIban())) {
            return Mono.error(new IllegalArgumentException("Source and target accounts must be different"));
        }

        // Normalizamos moneda; por defecto EUR
        String currency = (request.currency() == null || request.currency().isBlank())
                ? "EUR"
                : request.currency().toUpperCase();

       
        Mono<BigDecimal> amountInEurMono = ("EUR".equals(currency))
                ? Mono.just(request.amount())
                : exchangeRateClient.getRate(currency, "EUR")
                        .map(rate -> request.amount().multiply(rate.rate()))
                        .onErrorMap(ExchangeRateClient.ExchangeRateUnavailableException.class,
                                ex -> new ExchangeRateRequiredException("Exchange rate unavailable", ex));

        return amountInEurMono.flatMap(amountInEur -> Mono.zip(
                accountServiceClient.getAccountByIban(request.sourceIban()),
                accountServiceClient.getAccountByIban(request.targetIban()))
                .flatMap(tuple -> {
                    AccountDTO source = tuple.getT1();
                    ensureSufficientBalance(source.balance(), amountInEur);

                    return accountServiceClient.transfer(
                            request.sourceIban(),
                            request.targetIban(),
                            amountInEur)
                            .thenReturn(new TransferDTO(
                                    request.sourceIban(),
                                    request.targetIban(),
                                    amountInEur));
                }));
    }
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private void ensureSufficientBalance(BigDecimal currentBalance, BigDecimal amount) {
        if (currentBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
    }
}
