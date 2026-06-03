package br.com.escola.responsavel.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResponsavelVinculadoOutput(
        UUID id,
        String nomeCompleto,
        String cpf,
        String email,
        String telefone,
        String rg,
        String cep,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String parentesco,
        Boolean responsavelFinanceiro,
        Boolean responsavelPedagogico,
        Boolean autorizadoRetirar,
        LocalDateTime createdAt) {

    public static ResponsavelVinculadoOutput of(ResponsavelOutput responsavel, VinculoAlunoResponsavelOutput vinculo) {
        return new ResponsavelVinculadoOutput(
                responsavel.id(),
                responsavel.nomeCompleto(),
                responsavel.cpf(),
                responsavel.email(),
                responsavel.telefone(),
                responsavel.rg(),
                responsavel.cep(),
                responsavel.logradouro(),
                responsavel.numero(),
                responsavel.complemento(),
                responsavel.bairro(),
                responsavel.cidade(),
                responsavel.uf(),
                vinculo.parentesco(),
                vinculo.responsavelFinanceiro(),
                vinculo.responsavelPedagogico(),
                vinculo.autorizadoRetirar(),
                responsavel.createdAt());
    }
}
