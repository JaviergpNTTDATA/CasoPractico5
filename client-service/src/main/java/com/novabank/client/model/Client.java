package com.novabank.client.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("clients")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    @Id
    private Long id;

    @NotBlank(message = "Firstname is needed")
    private String firstName;

    @NotBlank(message = "Lastname is needed")
    private String lastName;

    @NotBlank(message = "DNI is needed")
    private String dni;

    @Email
    @NotBlank(message = "Email is needed")
    private String email;

    @NotBlank(message = "Phone is needed")
    private String phone;

    @Column("fecha_creacion")
    private LocalDateTime fechaCreacion;

    public Client(String firstName, String lastName, String dni, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dni = dni;
        this.phone = phone;
        this.email = email;
    }

}
