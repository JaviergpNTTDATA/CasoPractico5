package com.novabank.account.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novabank.account.dto.AccountBalanceDTO;
import com.novabank.account.dto.AccountDTO;
import com.novabank.account.dto.MovementDTO;
import com.novabank.account.service.AccountService;
import com.novabank.account.service.InquiryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Accounts", description = "Operations related to accounts")
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService service;
    private final InquiryService inquiryService;

    @Operation(summary = "Create account", description = "Creates a new account for a given client")
    @ApiResponse(responseCode = "200", description = "Account created successfully")
    @ApiResponse(responseCode = "404", description = "Client not found")
    @PostMapping("/create/{clientId}")
    public Mono<AccountDTO> create(@PathVariable Long clientId) {
        return service.createAccount(clientId);
    }

    @Operation(summary = "List accounts by client", description = "Returns all accounts associated with a client")
    @ApiResponse(responseCode = "200", description = "Accounts found")
    @ApiResponse(responseCode = "404", description = "Client not found")
    @GetMapping("/client/{clientId}")
    public Flux<AccountDTO> listByClient(@PathVariable Long clientId) {
        return service.listClientAccounts(clientId);
    }

    @Operation(summary = "Get account by IBAN", description = "Returns an account by its IBAN")
    @ApiResponse(responseCode = "200", description = "Account found")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @GetMapping("/iban/{iban}")
    public Mono<AccountDTO> getByIban(@PathVariable String iban) {
        return service.getAccountByIban(iban.toUpperCase());
    }

    @PostMapping("/{iban}/deposit")
    public Mono<MovementDTO> deposit(@PathVariable String iban, @RequestParam BigDecimal amount) {
        return service.deposit(iban.toUpperCase(), amount);
    }

    @PostMapping("/{iban}/withdraw")
    public Mono<MovementDTO> withdraw(@PathVariable String iban, @RequestParam BigDecimal amount) {
        return service.withdraw(iban.toUpperCase(), amount);
    }

    @Operation(summary = "Transfer money between accounts", description = "Transfers an amount from origin IBAN to destination IBAN")
    @ApiResponse(responseCode = "200", description = "Transfer completed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "404", description = "Origin or destination account not found")
    @ApiResponse(responseCode = "422", description = "Insufficient balance")
    @PostMapping("/transfer")
    public Mono<MovementDTO> transfer(
            @RequestParam String originIban,
            @RequestParam String destinationIban,
            @RequestParam BigDecimal amount) {
        return service.transfer(originIban.toUpperCase(), destinationIban.toUpperCase(), amount);
    }

    @Operation(summary = "Get movements of an account", description = "Returns all movements of an account ordered by date (desc)")
    @ApiResponse(responseCode = "200", description = "Movements found")
    @ApiResponse(responseCode = "404", description = "Account or movements not found")
    @GetMapping("/{iban}/movements")
    public Flux<MovementDTO> getMovementsByAccount(@PathVariable String iban) {
        return inquiryService.getByAccount(iban.toUpperCase());
    }

    @Operation(summary = "Get movements of an account between dates", description = "Returns movements of an account between two dates (inclusive), ordered by date (desc)")
    @ApiResponse(responseCode = "200", description = "Movements found")
    @ApiResponse(responseCode = "404", description = "Account or movements not found")
    @GetMapping("/{iban}/movements/by-date")
    public Flux<MovementDTO> getMovementsByAccountAndDates(
            @PathVariable String iban,
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return inquiryService.getByAccountAndDates(iban.toUpperCase(), start, end);
    }

    @Operation(summary = "Get account balance by IBAN", description = "Returns the current balance of an account by its IBAN")
    @ApiResponse(responseCode = "200", description = "Balance retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @GetMapping("/iban/{iban}/balance")
    public Mono<AccountBalanceDTO> getBalanceByIban(@PathVariable String iban) {
        return service.getBalanceByIban(iban.toUpperCase());
    }

}
