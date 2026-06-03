package br.com.escola.historico.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolarItem;

public interface HistoricoEscolarItemJpaRepository extends JpaRepository<HistoricoEscolarItem, UUID> {

    @Modifying
    @Query(value = """
            delete from historico_escolar_item item
             where item.id_historico_escolar in (
                   select h.id_historico_escolar
                     from historico_escolar h
                     join aluno a on a.id_aluno = h.id_aluno
                     join pessoa p on p.id_pessoa = a.id_pessoa
                    where lower(p.nome_completo) = lower(:nomeAluno)
                      and p.data_nascimento = :dataNascimento
             )
            """, nativeQuery = true)
    void deleteByAlunoDocumental(String nomeAluno, java.time.LocalDate dataNascimento);
}
