package com.novabank.client.exception;

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

    // Client not found 404
    @ExceptionHandler(ClientNotFoundException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleClientNotFound(ClientNotFoundException ex) {
        return Mono.just(buildResponse("CLIENT_NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    // Invalid or duplicated error 400
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleIllegalArgument(IllegalArgumentException ex) {
        return Mono.just(buildResponse("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    // Validation error 400
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

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    private ResponseEntity<Map<String, Object>> buildResponse(String errorCode, String message, HttpStatus status) {

        Map<String, Object> body = new HashMap<>();
        body.put("error", errorCode);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(body);
    }
}
