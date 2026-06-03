package br.com.escola.historico.adapter.out.persistence.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolar;

public interface HistoricoEscolarJpaRepository extends JpaRepository<HistoricoEscolar, UUID> {

    @EntityGraph(attributePaths = "componentesCurriculares")
    @Query("select h from HistoricoEscolar h where h.id = :id")
    Optional<HistoricoEscolar> findWithComponentesCurricularesById(UUID id);

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
