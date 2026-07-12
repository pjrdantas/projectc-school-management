package br.com.escola.professor.application.port.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.professor.application.dto.internal.AlocarProfessorTurmaDisciplinaSolicitacao;
import br.com.escola.professor.application.dto.internal.CriarProfessorSolicitacao;
import br.com.escola.professor.application.dto.internal.ProfessorAlocacaoResumo;
import br.com.escola.professor.application.dto.internal.ProfessorFuncionarioElegivelResumo;
import br.com.escola.professor.application.dto.internal.ProfessorResumo;

public interface ProfessorAcademicoPort {

    ProfessorResumo criarProfessor(UUID escolaId, CriarProfessorSolicitacao solicitacao);

    List<ProfessorResumo> listarProfessores(UUID escolaId);

    Optional<ProfessorResumo> buscarProfessor(UUID escolaId, UUID professorId);

    boolean existeProfessor(UUID escolaId, UUID professorId);

    ProfessorAlocacaoResumo alocarProfessorTurmaDisciplina(
            UUID escolaId,
            UUID professorId,
            AlocarProfessorTurmaDisciplinaSolicitacao solicitacao);

    List<ProfessorAlocacaoResumo> listarAlocacoes(UUID escolaId, UUID professorId);

    List<ProfessorAlocacaoResumo> listarProfessoresPorTurma(UUID escolaId, UUID turmaId);

    List<ProfessorFuncionarioElegivelResumo> listarFuncionariosElegiveis(UUID escolaId);
}
