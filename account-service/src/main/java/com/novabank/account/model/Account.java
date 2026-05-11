package com.novabank.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Table("accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    private Long id;

    @Column("iban")
    private String iban;

    @Column("client_id")
    private Long clientId;

    /**
     * En R2DBC no existen relaciones ORM tipo @OneToMany.
     * Los movimientos se consultan vía MovementRepository por account_id.
     */
    @Column("balance")
    private BigDecimal balance;

    @Column("created_at")
    private LocalDateTime createdAt;

    public Account(Long clientId) {
        this.clientId = clientId;
        this.balance = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public void ensureDefaults() {
        if (balance == null) balance = BigDecimal.ZERO;
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        ensureDefaults();
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        ensureDefaults();
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }
}
