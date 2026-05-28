package br.com.escola.shared.document.adapter.out.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import br.com.escola.shared.document.adapter.out.persistence.entity.DocumentoEntity;

public interface DocumentoJpaRepository extends JpaRepository<DocumentoEntity, UUID> {

    @Query(value = """
            SELECT d.*
              FROM documento d
              JOIN pessoa_documento pd ON pd.id_documento = d.id_documento
             WHERE pd.id_pessoa = :pessoaId
             ORDER BY d.data_upload DESC
            """, nativeQuery = true)
    List<DocumentoEntity> findByPessoaIdOrderByDataUploadDesc(UUID pessoaId);

    @Modifying
    @Query(value = "DELETE FROM pessoa_documento WHERE id_documento = :documentoId", nativeQuery = true)
    void deletePessoaDocumentoByDocumentoId(UUID documentoId);

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
