package br.com.escola.compartilhado.pessoa.dto.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PessoaAlunoResponsaveisResumo(
        UUID idAluno,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        LocalDate dataNascimento,
        LocalDateTime createdAt,
        List<PessoaResponsavelResumo> responsaveis) {
}
