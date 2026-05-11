package com.novabank.account.mapper;

import com.novabank.account.dto.MovementDTO;
import com.novabank.account.model.Account;
import com.novabank.account.model.Movement;

public class MovementMapper {

    /**
     * En R2DBC Movement ya no tiene referencia a Account (no hay @ManyToOne).
     * Para incluir el IBAN en el DTO se necesita el Account (o el iban) aparte.
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
