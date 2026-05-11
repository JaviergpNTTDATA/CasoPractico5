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
@Table("movements")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Movement {

    @Id
    private Long id;

    @Column("account_id")
    private Long accountId;

    @Column("type")
    private MovementType type;

    @Column("amount")
    private BigDecimal amount;

    @Column("created_at")
    private LocalDateTime createdAt;

    public void ensureDefaults() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
