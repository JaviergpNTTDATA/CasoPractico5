package com.novabank.account.mapper;

import com.novabank.account.dto.MovementDTO;
import com.novabank.account.model.Account;
import com.novabank.account.model.Movement;

public class MovementMapper {

    /**
     * In R2DBC, Movement no longer has a reference to Account (no @ManyToOne).
     * To include the IBAN in the DTO, the Account (or the iban) is needed separately.
     */
    public static MovementDTO toDto(Movement movement, Account account) {
        if (movement == null) {
            return null;
        }

        String iban = account != null ? account.getIban() : null;

        return new MovementDTO(
                movement.getId(),
                iban,
                movement.getType() != null ? movement.getType().name() : null,
                movement.getAmount(),
                movement.getCreatedAt()
        );
    }
}
