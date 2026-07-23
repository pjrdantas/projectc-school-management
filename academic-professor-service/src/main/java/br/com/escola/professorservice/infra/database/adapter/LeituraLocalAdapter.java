package br.com.escola.professorservice.infra.database.adapter;

import static br.com.escola.professorservice.infra.database.mapper.AlocacaoReadMapper.toResponse;
import static br.com.escola.professorservice.infra.database.mapper.PersistenciaMapper.toResponse;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.escola.professorservice.application.context.InternalRequestContext;
import br.com.escola.professorservice.application.dto.AlocacaoResponse;
import br.com.escola.professorservice.application.dto.ResumoResponse;
import br.com.escola.professorservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.professorservice.infra.database.entity.AlocacaoJpaEntity;
import br.com.escola.professorservice.infra.database.entity.CadastroJpaEntity;
import br.com.escola.professorservice.infra.database.repository.AlocacaoJpaRepository;
import br.com.escola.professorservice.infra.database.repository.CadastroJpaRepository;
import br.com.escola.professorservice.application.port.out.LeituraLocalPort;

@Repository
public class LeituraLocalAdapter implements LeituraLocalPort {

    private final CadastroJpaRepository professorRepository;
    private final AlocacaoJpaRepository alocacaoRepository;

    public LeituraLocalAdapter(
            CadastroJpaRepository professorRepository,
            AlocacaoJpaRepository alocacaoRepository) {
        this.professorRepository = professorRepository;
        this.alocacaoRepository = alocacaoRepository;
    }

    @Override
    public List<ResumoResponse> listarProfessores(InternalRequestContext context) {
        return professorRepository.findAllByEscolaIdOrderByNomeCompletoAscIdAsc(context.escolaId()).stream()
                .map(professor -> toResponse(professor))
                .toList();
    }

    @Override
    public ResumoResponse buscarProfessorPorId(InternalRequestContext context, UUID professorId) {
        return professorRepository.findById(professorId)
                .filter(professor -> professor.getEscolaId().equals(context.escolaId()))
                .map(professor -> toResponse(professor))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado"));
    }

    @Override
    public List<AlocacaoResponse> listarAlocacoes(InternalRequestContext context, UUID professorId) {
        CadastroJpaEntity professor = professorRepository.findById(professorId)
                .filter(entity -> entity.getEscolaId().equals(context.escolaId()))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor não encontrado"));

        return alocacaoRepository.findAllByProfessorIdOrderByCreatedAtAsc(professorId).stream()
                .map(entity -> toResponse(entity, professor))
                .toList();
    }

    @Override
    public List<AlocacaoResponse> listarProfessoresPorTurma(InternalRequestContext context, UUID turmaId) {
        return alocacaoRepository.findAllByTurmaIdOrderByCreatedAtAsc(turmaId).stream()
                .map(entity -> mapearSeProfessorDaMesmaEscola(context, entity))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    private AlocacaoResponse mapearSeProfessorDaMesmaEscola(
            InternalRequestContext context,
            AlocacaoJpaEntity entity) {
        return professorRepository.findById(entity.getProfessorId())
                .filter(professor -> professor.getEscolaId().equals(context.escolaId()))
                .map(professor -> toResponse(entity, professor))
                .orElse(null);
    }
}

