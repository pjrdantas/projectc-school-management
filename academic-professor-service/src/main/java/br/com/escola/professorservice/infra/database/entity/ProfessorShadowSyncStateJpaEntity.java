package br.com.escola.professorservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "professor_shadow_sync_state")
public class ProfessorShadowSyncStateJpaEntity {

    @Id
    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @Column(name = "professores_completos", nullable = false)
    private Boolean professoresCompletos;

    @Column(name = "professor_count", nullable = false)
    private Long professorCount;

    @Column(name = "sincronizado_em", nullable = false)
    private LocalDateTime sincronizadoEm;

    protected ProfessorShadowSyncStateJpaEntity() {
    }

    public ProfessorShadowSyncStateJpaEntity(
            UUID escolaId,
            Boolean professoresCompletos,
            Long professorCount,
            LocalDateTime sincronizadoEm) {
        this.escolaId = escolaId;
        this.professoresCompletos = professoresCompletos;
        this.professorCount = professorCount;
        this.sincronizadoEm = sincronizadoEm;
    }

    public UUID getEscolaId() {
        return escolaId;
    }

    public Boolean getProfessoresCompletos() {
        return professoresCompletos;
    }

    public Long getProfessorCount() {
        return professorCount;
    }

    public LocalDateTime getSincronizadoEm() {
        return sincronizadoEm;
    }
}
