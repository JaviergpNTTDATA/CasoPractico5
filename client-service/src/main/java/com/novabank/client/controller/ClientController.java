package com.novabank.client.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.novabank.client.dto.ClientDTO;
import com.novabank.client.dto.CreateClient;
import com.novabank.client.service.ClientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Clients", description = "Operations related to clients")
@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ClientDTO> create(@Valid @RequestBody CreateClient dto) {
        return service.createClient(dto);
    }

    @Operation(summary = "Search client by ID", description = "Returns a client based on the provided ID")
    @ApiResponse(responseCode = "200", description = "Client found")
    @ApiResponse(responseCode = "404", description = "Client not found")
    @GetMapping("/getById/{id}")
    public Mono<ClientDTO> searchById(@PathVariable Long id) {
        return service.getClient(id);
    }

    @Operation(summary = "Search client by DNI", description = "Returns a client based on the provided DNI")
    @ApiResponse(responseCode = "200", description = "Client found")
    @ApiResponse(responseCode = "404", description = "Client not found")
    @GetMapping("/getByDni/{dni}")
    public Mono<ClientDTO> searchByDNI(@PathVariable String dni) {
        return service.getClientByDni(dni.toUpperCase());
    }

    @Operation(summary = "List all clients", description = "Returns all registered clients")
    @ApiResponse(responseCode = "200", description = "Clients retrieved successfully")
    @GetMapping("/getAll")
    public Flux<ClientDTO> getAll() {
        return service.listClients();
    }
}
