package br.com.escola.peopleservice.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PessoaAlunoResponsaveisResponse(
        UUID idAluno,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDate dataNascimento,
        LocalDateTime createdAt,
        List<PessoaResponsavelResumoResponse> responsaveis) {
}
