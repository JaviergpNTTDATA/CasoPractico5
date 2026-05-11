package com.novabank.operation.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.novabank.operation.client.AccountServiceClient;
import com.novabank.operation.client.ExchangeRateClient;
import com.novabank.operation.dto.AccountDTO;
import com.novabank.operation.dto.DepositWithdrawDTO;
import com.novabank.operation.dto.DepositWithdrawRequest;
import com.novabank.operation.dto.MovementDTO;
import com.novabank.operation.dto.TransferDTO;
import com.novabank.operation.dto.TransferRequest;
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

        // Ejemplo: consultamos el tipo de cambio antes de ejecutar la transferencia.
        // Fallback seguro: si el servicio de divisas falla, devolvemos error controlado (503)
        // en vez de continuar silenciosamente con una tasa incorrecta.
        Mono<ExchangeRateClient.ExchangeRateResponse> rateMono = exchangeRateClient
                .getRate("EUR", "EUR")
                .onErrorMap(ExchangeRateClient.ExchangeRateUnavailableException.class,
                        ex -> new ExchangeRateRequiredException("Exchange rate unavailable", ex));

        return rateMono.then(Mono.zip(
                accountServiceClient.getAccountByIban(request.sourceIban()),
                accountServiceClient.getAccountByIban(request.targetIban()))
                .flatMap(tuple -> {
                    AccountDTO source = tuple.getT1();
                    ensureSufficientBalance(source.balance(), request.amount());

                    return accountServiceClient.transfer(
                            request.sourceIban(),
                            request.targetIban(),
                            request.amount())
                            .thenReturn(new TransferDTO(
                                    request.sourceIban(),
                                    request.targetIban(),
                                    request.amount()));
                }));
    }

    /**
     * Error específico para que el GlobalExceptionHandler lo traduzca a 503.
     */
    public static class ExchangeRateRequiredException extends RuntimeException {
        public ExchangeRateRequiredException(String message, Throwable cause) {
            super(message, cause);
        }
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
