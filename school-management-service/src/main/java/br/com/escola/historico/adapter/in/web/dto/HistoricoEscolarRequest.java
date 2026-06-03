package br.com.escola.historico.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record HistoricoEscolarRequest(
        @NotBlank(message = "nomeAluno é obrigatório")
        String nomeAluno,
        String rgRen,
        String ra,
        String rm,
        LocalDate dataNascimento,
        String municipioNascimento,
        String estadoNascimento,
        String paisNascimento,
        String nomeEscola,
        String enderecoEscola,
        String municipioEscola,
        String cepEscola,
        String telefoneEscola,
        @Email(message = "emailEscola deve ser válido")
        String emailEscola,
        @PositiveOrZero(message = "anoConclusao não pode ser negativo")
        Integer anoConclusao,
        String ensinoConcluido,
        LocalDate dataEmissao,
        String diretorNome,
        String diretorRg,
        String gerenteOrganizacaoNome,
        String gerenteOrganizacaoRg,
        String doeNumero,
        LocalDate doeData,
        String doeVolume,
        String doePagina,
        String observacoes,
        @Valid
        List<HistoricoEscolarItemRequest> componentesCurriculares) {
}
