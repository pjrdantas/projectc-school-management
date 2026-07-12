package br.com.escola.professorservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "professor_turma_shadow_sync_state")
public class ProfessorTurmaShadowSyncStateJpaEntity {

    @Id
    @Column(name = "id_turma", nullable = false)
    private UUID turmaId;

    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @Column(name = "alocacoes_completas", nullable = false)
    private Boolean alocacoesCompletas;

    @Column(name = "alocacao_count", nullable = false)
    private Long alocacaoCount;

    @Column(name = "sincronizado_em", nullable = false)
    private LocalDateTime sincronizadoEm;

    protected ProfessorTurmaShadowSyncStateJpaEntity() {
    }

    public ProfessorTurmaShadowSyncStateJpaEntity(
            UUID turmaId,
            UUID escolaId,
            Boolean alocacoesCompletas,
            Long alocacaoCount,
            LocalDateTime sincronizadoEm) {
        this.turmaId = turmaId;
        this.escolaId = escolaId;
        this.alocacoesCompletas = alocacoesCompletas;
        this.alocacaoCount = alocacaoCount;
        this.sincronizadoEm = sincronizadoEm;
    }

    public UUID getTurmaId() {
        return turmaId;
    }

    public UUID getEscolaId() {
        return escolaId;
    }

    public Boolean getAlocacoesCompletas() {
        return alocacoesCompletas;
    }

    public Long getAlocacaoCount() {
        return alocacaoCount;
    }

    public LocalDateTime getSincronizadoEm() {
        return sincronizadoEm;
    }
}
