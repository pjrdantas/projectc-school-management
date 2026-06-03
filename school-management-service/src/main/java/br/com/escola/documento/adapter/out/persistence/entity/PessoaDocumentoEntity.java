package br.com.escola.documento.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pessoa_documento")
public class PessoaDocumentoEntity {

    @Id
    @Column(name = "id_pessoa_documento", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "id_pessoa", nullable = false)
    private UUID pessoaId;

    @Column(name = "id_documento", nullable = false)
    private UUID documentoId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
