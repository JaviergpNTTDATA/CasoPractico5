package com.novabank.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.novabank.auth.model.User;
import com.novabank.auth.repository.UserRepository;

@SpringBootApplication
public class AuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initTestUser(UserRepository userRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            String username = "ADMIN_USER";
            String rawPassword = "ADMIN_PASSWORD";

            userRepository.findByUsername(username)
                    .switchIfEmpty(userRepository.save(buildUser(username, rawPassword, passwordEncoder)))
                    .then()
                    .subscribe();

            // Nota: lo imprimimos siempre, para no bloquear en la fase de arranque.
            System.out.println("Auth user seed ensured: " + username);
        };
    }

    private static User buildUser(String username, String rawPassword, PasswordEncoder passwordEncoder) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole("ROLE_USER");
        return user;
    }
}
