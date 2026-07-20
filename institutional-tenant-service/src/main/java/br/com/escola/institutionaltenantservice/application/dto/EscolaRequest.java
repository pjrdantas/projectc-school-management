package br.com.escola.institutionaltenantservice.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EscolaRequest(
        @NotBlank @Size(max = 150) String nome,
        @Size(max = 30) String codigoInep,
        @Size(max = 18) String cnpj,
        @Size(max = 30) String telefone,
        @Email @Size(max = 150) String email,
        UUID enderecoId,
        Boolean ativo) {
}
