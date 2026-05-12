package com.novabank.operation.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.novabank.operation.client.AccountServiceClient;
import com.novabank.operation.client.ExchangeRateClient;
import com.novabank.operation.dto.AccountDTO;
import com.novabank.operation.dto.MovementDTO;
import com.novabank.operation.dto.TransferRequest;
import com.novabank.operation.exception.ExchangeRateRequiredException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationServiceTest {

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @InjectMocks
    private OperationService operationService;

    @Test
    void transfer_withEnoughBalance_shouldReturnTransferDto() {
        TransferRequest request = new TransferRequest(
                "ESFROM",
                "ESTO",
                BigDecimal.TEN,
                "USD"
        );

        AccountDTO source = new AccountDTO(
                1L,
                "ESFROM",
                BigDecimal.valueOf(100)
        );

        AccountDTO target = new AccountDTO(
                2L,
                "ESTO",
                BigDecimal.ZERO
        );

        // La implementación actual consulta divisa antes de transferir.
        when(exchangeRateClient.getRate("USD", "EUR")).thenReturn(Mono.just(
                new ExchangeRateClient.ExchangeRateResponse("USD", "EUR", new BigDecimal("0.90"), java.time.Instant.now())
        ));

        when(accountServiceClient.getAccountByIban("ESFROM")).thenReturn(Mono.just(source));
        when(accountServiceClient.getAccountByIban("ESTO")).thenReturn(Mono.just(target));

        // 10 USD * 0.90 = 9 EUR (importe que se debe usar en account-service)
        BigDecimal expectedEur = new BigDecimal("9.00");

        // account-service transfer devuelve un MovementDTO (ver AccountServiceClient.transfer)
        when(accountServiceClient.transfer("ESFROM", "ESTO", expectedEur))
                .thenReturn(Mono.just(new MovementDTO(
                        1L,
                        "ESFROM",
                        "TRANSFER",
                        expectedEur,
                        java.time.LocalDateTime.now()
                )));

        StepVerifier.create(operationService.transfer(request))
                .assertNext(result -> {
                    org.junit.jupiter.api.Assertions.assertEquals("ESFROM", result.sourceIban());
                    org.junit.jupiter.api.Assertions.assertEquals("ESTO", result.targetIban());
                    org.junit.jupiter.api.Assertions.assertEquals(0, new BigDecimal("9.00").compareTo(result.amount()));
                })
                .verifyComplete();

        verify(exchangeRateClient).getRate("USD", "EUR");
        verify(accountServiceClient).getAccountByIban("ESFROM");
        verify(accountServiceClient).getAccountByIban("ESTO");
        verify(accountServiceClient).transfer("ESFROM", "ESTO", new BigDecimal("9.00"));
        verifyNoMoreInteractions(accountServiceClient, exchangeRateClient);
    }

    @Test
    void transfer_whenExchangeRateUnavailable_shouldFailAndNotCallAccountTransfer() {
        TransferRequest request = new TransferRequest(
                "ESFROM",
                "ESTO",
                BigDecimal.TEN,
                "USD"
        );

        when(exchangeRateClient.getRate("USD", "EUR"))
                .thenReturn(Mono.error(new ExchangeRateClient.ExchangeRateUnavailableException("down")));

        StepVerifier.create(operationService.transfer(request))
                .expectError(ExchangeRateRequiredException.class)
                .verify();

        // Importante: no debe tocar account-service si falla el rate (fallback seguro)
        verify(exchangeRateClient).getRate("USD", "EUR");
        verifyNoInteractions(accountServiceClient);
        verifyNoMoreInteractions(exchangeRateClient);
    }
}
