package br.com.escola.dashboardqueryservice.infra.persistence.jpa.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.dashboardqueryservice.application.dto.TipoPainelProjecao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "painel_projecao")
public class PainelProjecaoJpaEntity {

    @Id
    private UUID id;

    @Column(name = "escola_id", nullable = false)
    private UUID escolaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private TipoPainelProjecao tipo;

    @Column(name = "chave_projecao", nullable = false, length = 500)
    private String chaveProjecao;

    @Column(name = "publico_codigo", length = 100)
    private String publicoCodigo;

    @Column(name = "professor_id")
    private UUID professorId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "referencia_data")
    private LocalDate referenciaData;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected PainelProjecaoJpaEntity() {
    }

    public PainelProjecaoJpaEntity(UUID id, UUID escolaId, TipoPainelProjecao tipo, String chaveProjecao) {
        this.id = id;
        this.escolaId = escolaId;
        this.tipo = tipo;
        this.chaveProjecao = chaveProjecao;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEscolaId() {
        return escolaId;
    }

    public TipoPainelProjecao getTipo() {
        return tipo;
    }

    public String getChaveProjecao() {
        return chaveProjecao;
    }

    public String getPublicoCodigo() {
        return publicoCodigo;
    }

    public UUID getProfessorId() {
        return professorId;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public LocalDate getReferenciaData() {
        return referenciaData;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void atualizar(
            String publicoCodigo,
            UUID professorId,
            UUID usuarioId,
            LocalDate referenciaData,
            String payload,
            LocalDateTime updatedAt) {
        this.publicoCodigo = publicoCodigo;
        this.professorId = professorId;
        this.usuarioId = usuarioId;
        this.referenciaData = referenciaData;
        this.payload = payload;
        this.updatedAt = updatedAt;
    }
}
