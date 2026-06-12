package br.com.escola.historico.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolar;

public interface HistoricoEscolarJpaRepository extends JpaRepository<HistoricoEscolar, UUID> {

    long countByOrigemIgnoreCase(String origem);

    long countByOrigemIgnoreCaseAndAluno_Pessoa_Escola_Id(String origem, UUID escolaId);

    @EntityGraph(attributePaths = { "componentesCurriculares", "aluno", "aluno.pessoa" })
    @Query("select h from HistoricoEscolar h where h.id = :id")
    Optional<HistoricoEscolar> findWithComponentesCurricularesById(UUID id);

    @EntityGraph(attributePaths = { "componentesCurriculares", "aluno", "aluno.pessoa", "aluno.pessoa.escola" })
    @Query("""
            select h
              from HistoricoEscolar h
              join h.aluno a
              join a.pessoa p
             where h.id = :id
               and p.escola.id = :escolaId
            """)
    Optional<HistoricoEscolar> findWithComponentesCurricularesByIdAndEscolaId(UUID id, UUID escolaId);

    @EntityGraph(attributePaths = { "componentesCurriculares", "aluno", "aluno.pessoa" })
    List<HistoricoEscolar> findByAlunoId(UUID alunoId);

    @EntityGraph(attributePaths = { "componentesCurriculares", "aluno", "aluno.pessoa", "aluno.pessoa.escola" })
    List<HistoricoEscolar> findByAlunoIdAndAluno_Pessoa_Escola_Id(UUID alunoId, UUID escolaId);

    @EntityGraph(attributePaths = { "aluno", "aluno.pessoa", "aluno.pessoa.escola" })
    Page<HistoricoEscolar> findByAluno_Pessoa_Escola_Id(UUID escolaId, Pageable pageable);

    @EntityGraph(attributePaths = { "componentesCurriculares", "aluno", "aluno.pessoa" })
    @Query("""
            select distinct h
              from HistoricoEscolar h
              join h.componentesCurriculares item
             where h.alunoId = :alunoId
               and item.periodoLetivo.id = :periodoLetivoId
            """)
    List<HistoricoEscolar> findByAlunoIdAndPeriodoLetivoId(UUID alunoId, UUID periodoLetivoId);

    @EntityGraph(attributePaths = { "componentesCurriculares", "aluno", "aluno.pessoa", "aluno.pessoa.escola" })
    @Query("""
            select distinct h
              from HistoricoEscolar h
              join h.aluno a
              join a.pessoa p
              join h.componentesCurriculares item
             where h.alunoId = :alunoId
               and p.escola.id = :escolaId
               and item.periodoLetivo.id = :periodoLetivoId
            """)
    List<HistoricoEscolar> findByAlunoIdAndEscolaIdAndPeriodoLetivoId(
            UUID alunoId,
            UUID escolaId,
            UUID periodoLetivoId);

    @Modifying
    @Query(value = """
            delete from historico_escolar h
             where h.id_aluno in (
                   select a.id_aluno
                     from aluno a
                     join pessoa p on p.id_pessoa = a.id_pessoa
                    where lower(p.nome_completo) = lower(:nomeAluno)
                      and p.data_nascimento = :dataNascimento
             )
            """, nativeQuery = true)
    void deleteByAlunoDocumental(String nomeAluno, LocalDate dataNascimento);
}
