package br.com.escola.ia.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.escola.ia.adapter.out.persistence.entity.BibliotecaConteudoPedagogicoEntity;

public interface BibliotecaConteudoPedagogicoJpaRepository extends JpaRepository<BibliotecaConteudoPedagogicoEntity, UUID> {

    List<BibliotecaConteudoPedagogicoEntity> findByProfessorId(UUID professorId);

    @Query("""
            SELECT conteudo
            FROM BibliotecaConteudoPedagogicoEntity conteudo
            LEFT JOIN conteudo.professor professor
            LEFT JOIN conteudo.disciplina disciplina
            LEFT JOIN conteudo.tipoConteudoIA tipo
            WHERE conteudo.ativo = true
              AND (:professorId IS NULL OR professor.id = :professorId)
              AND (:disciplinaId IS NULL OR disciplina.id = :disciplinaId)
              AND (:tipoConteudo IS NULL OR tipo.codigo = :tipoConteudo)
              AND (:tema IS NULL OR LOWER(conteudo.tema) LIKE LOWER(CONCAT('%', :tema, '%')))
            ORDER BY conteudo.createdAt DESC
            """)
    List<BibliotecaConteudoPedagogicoEntity> filtrar(
            @Param("professorId") UUID professorId,
            @Param("disciplinaId") UUID disciplinaId,
            @Param("tipoConteudo") String tipoConteudo,
            @Param("tema") String tema);
}
