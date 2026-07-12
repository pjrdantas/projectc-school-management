package br.com.escola.professor.adapter.in.web.dto;

public record DiarioClasseCabecalhoResponse(
        String idDiarioClasse,
        String idEscola,
        String escola,
        String diretoriaEnsino,
        String municipio,
        Integer anoLetivo,
        Integer mes,
        String dataAtual,
        String idTurma,
        String turmaSerie,
        String turno,
        String idDisciplina,
        String disciplina,
        String idProfessor,
        String professor) {
}
