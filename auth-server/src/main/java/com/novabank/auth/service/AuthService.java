package com.novabank.auth.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.novabank.auth.model.User;
import com.novabank.auth.repository.UserRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // clave simétrica simple para el módulo; en real, en config segura
    private final String jwtSecret = "mi-clave-super-secreta-para-novabank";

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<String> authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
                        return Mono.error(new RuntimeException("Invalid credentials"));
                    }

                    Instant now = Instant.now();
                    Instant expiry = now.plus(1, ChronoUnit.HOURS);

                    String token = Jwts.builder()
                            .setSubject(user.getUsername())
                            .claim("role", user.getRole())
                            .setIssuedAt(Date.from(now))
                            .setExpiration(Date.from(expiry))
                            .signWith(SignatureAlgorithm.HS256, jwtSecret.getBytes())
                            .compact();

                    return Mono.just(token);
                });
    }

    public String getJwtSecret() {
        return jwtSecret;
    }
}
