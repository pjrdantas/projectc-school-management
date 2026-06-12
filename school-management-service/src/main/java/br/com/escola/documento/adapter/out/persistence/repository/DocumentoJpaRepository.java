package br.com.escola.documento.adapter.out.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import br.com.escola.documento.adapter.out.persistence.entity.DocumentoEntity;

public interface DocumentoJpaRepository extends JpaRepository<DocumentoEntity, UUID> {

    @Query(value = """
            SELECT d.*
              FROM documento d
              JOIN pessoa_documento pd ON pd.id_documento = d.id_documento
             WHERE pd.id_pessoa = :pessoaId
             ORDER BY d.data_upload DESC
            """, nativeQuery = true)
    List<DocumentoEntity> findByPessoaIdOrderByDataUploadDesc(UUID pessoaId);

    @Query(value = """
            SELECT d.*
              FROM documento d
              JOIN pessoa_documento pd ON pd.id_documento = d.id_documento
              JOIN pessoa p ON p.id_pessoa = pd.id_pessoa
             WHERE d.id_documento = :documentoId
               AND p.id_escola = :escolaId
             LIMIT 1
            """, nativeQuery = true)
    Optional<DocumentoEntity> findByIdAndEscolaId(UUID documentoId, UUID escolaId);

    @Query(value = """
            SELECT d.*
              FROM documento d
              JOIN pessoa_documento pd ON pd.id_documento = d.id_documento
              JOIN pessoa p ON p.id_pessoa = pd.id_pessoa
             WHERE pd.id_pessoa = :pessoaId
               AND p.id_escola = :escolaId
             ORDER BY d.data_upload DESC
            """, nativeQuery = true)
    List<DocumentoEntity> findByPessoaIdAndEscolaIdOrderByDataUploadDesc(UUID pessoaId, UUID escolaId);

    @Query(value = """
            SELECT COUNT(1) > 0
              FROM documento d
              JOIN pessoa_documento pd ON pd.id_documento = d.id_documento
              JOIN pessoa p ON p.id_pessoa = pd.id_pessoa
             WHERE d.id_documento = :documentoId
               AND p.id_escola = :escolaId
            """, nativeQuery = true)
    boolean existsByIdAndEscolaId(UUID documentoId, UUID escolaId);

    @Modifying
    @Query(value = "DELETE FROM pessoa_documento WHERE id_documento = :documentoId", nativeQuery = true)
    void deletePessoaDocumentoByDocumentoId(UUID documentoId);

    @Modifying
    @Query(value = """
            DELETE FROM pessoa_documento pd
             WHERE pd.id_documento = :documentoId
               AND EXISTS (
                   SELECT 1
                     FROM pessoa p
                    WHERE p.id_pessoa = pd.id_pessoa
                      AND p.id_escola = :escolaId
               )
            """, nativeQuery = true)
    void deletePessoaDocumentoByDocumentoIdAndEscolaId(UUID documentoId, UUID escolaId);

    @Modifying
    @Query(value = "DELETE FROM pessoa_documento WHERE id_pessoa = :pessoaId", nativeQuery = true)
    void deletePessoaDocumentoByPessoaId(UUID pessoaId);

    @Modifying
    @Query(value = """
            DELETE FROM documento d
             WHERE NOT EXISTS (
                   SELECT 1 FROM pessoa_documento pd WHERE pd.id_documento = d.id_documento
             )
            """, nativeQuery = true)
    void deleteDocumentosSemVinculo();
}
