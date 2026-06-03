package br.com.escola.historico.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record HistoricoEscolarResponse(
        UUID id,
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
        String emailEscola,
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
        List<HistoricoEscolarItemResponse> componentesCurriculares) {
}
