package com.novabank.account.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClientNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleClienteNotFound(ClientNotFoundException ex) {
        return Mono.just(buildResponse("CLIENT_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(MovementNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleMovementNotFound(MovementNotFoundException ex) {
        return Mono.just(buildResponse("MOVEMENT_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleCuentaNotFound(AccountNotFoundException ex) {
        return Mono.just(buildResponse("ACCOUNT_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleSaldoInsuficiente(InsufficientBalanceException ex) {
        return Mono.just(buildResponse("INSUFFICIENT_BALANCE", ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgument(IllegalArgumentException ex) {
        return Mono.just(buildResponse("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("error", "VALIDATION_ERROR");
        body.put("messages", errores);
        body.put("timestamp", LocalDateTime.now());

        return Mono.just(new ResponseEntity<>(body, HttpStatus.BAD_REQUEST));
    }

    private ResponseEntity<Map<String, Object>> buildResponse(String errorCode, String message, HttpStatus status) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", errorCode);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());
        return new ResponseEntity<>(body, status);
    }
}
