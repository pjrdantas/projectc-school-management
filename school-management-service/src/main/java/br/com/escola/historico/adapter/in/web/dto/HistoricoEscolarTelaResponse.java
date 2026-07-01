package br.com.escola.historico.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record HistoricoEscolarTelaResponse(
        Contexto contexto,
        Cabecalho cabecalho,
        Aluno aluno,
        List<Periodo> periodos,
        List<Componente> baseComum,
        List<Componente> parteDiversificada,
        Totais totais,
        List<EstudoRealizado> estudosRealizados,
        String observacoes,
        Certificado certificado,
        List<Pendencia> pendencias) {

    public record Contexto(
            UUID idHistoricoEscolar,
            UUID idAluno,
            UUID idMatricula,
            String modo,
            String status,
            Integer serieMatriculaAtual,
            Integer serieConcluidaOrigem,
            String escolaOrigem,
            String dataTransferencia,
            boolean bloqueado) {
    }

    public record Cabecalho(
            String governo,
            String secretaria,
            String diretoria,
            String escola,
            String atoLegal,
            String atoLegalCriacao,
            String endereco,
            String numero,
            String bairro,
            String municipio,
            String cep,
            String telefone,
            String email) {
    }

    public record Aluno(
            String nome,
            String rg,
            String ra,
            String nascimentoMunicipio,
            String nascimentoEstado,
            String nascimentoPais,
            String nascimentoData) {
    }

    public record Periodo(
            Integer ordem,
            String anoLetivo,
            String serie,
            String equivalencia) {
    }

    public record Componente(
            Integer ordem,
            String nome,
            List<String> valores) {
    }

    public record Totais(
            List<String> totalBaseComum,
            List<String> totalParteDiversificada,
            List<String> totalAulasAnuais,
            List<String> totalCargaHoraria) {
    }

    public record EstudoRealizado(
            Integer ordem,
            String serieAno,
            String ano,
            String escola,
            String municipio,
            String uf) {
    }

    public record Certificado(
            Integer serieConcluida,
            String diretor,
            String escola,
            String rgAluno,
            String ano,
            String publicacao,
            String data,
            String gerenteNome,
            String gerenteRg,
            String diretorNome,
            String diretorRg) {
    }

    public record Pendencia(
            String codigo,
            String severidade,
            String aba,
            String mensagem) {
    }
}
