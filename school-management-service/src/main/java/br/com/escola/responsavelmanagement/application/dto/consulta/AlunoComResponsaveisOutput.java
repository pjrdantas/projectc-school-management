package br.com.escola.responsavelmanagement.application.dto.consulta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AlunoComResponsaveisOutput(
        UUID idAluno,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDate dataNascimento,
        LocalDateTime createdAt,
        List<ResponsavelResumoOutput> responsaveis) {
}
