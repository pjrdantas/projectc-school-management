package br.com.escola.transfermanagement.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "status_transferencia")
public class StatusTransferenciaEntity {

    @Id
    @Column(name = "id_status_transferencia", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, length = 80)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 150)
    private String descricao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
