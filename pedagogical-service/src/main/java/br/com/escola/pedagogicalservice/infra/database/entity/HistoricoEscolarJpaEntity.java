package br.com.escola.pedagogicalservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "academic_history_record")
public class HistoricoEscolarJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "student_id")
    private UUID alunoId;

    @Column(name = "enrollment_id")
    private UUID matriculaId;

    @Column(name = "screen_mode", length = 40)
    private String modo;

    @Lob
    @Column(name = "screen_payload_json")
    private String payloadTela;

    @Lob
    @Column(name = "write_payload_json")
    private String payloadEscrita;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected HistoricoEscolarJpaEntity() {
    }

    public HistoricoEscolarJpaEntity(
            UUID id,
            UUID schoolId,
            UUID alunoId,
            UUID matriculaId,
            String modo,
            String payloadTela,
            String payloadEscrita,
            LocalDateTime updatedAt) {
        this.id = id;
        this.schoolId = schoolId;
        this.alunoId = alunoId;
        this.matriculaId = matriculaId;
        this.modo = modo;
        this.payloadTela = payloadTela;
        this.payloadEscrita = payloadEscrita;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getAlunoId() { return alunoId; }
    public UUID getMatriculaId() { return matriculaId; }
    public String getModo() { return modo; }
    public String getPayloadTela() { return payloadTela; }
    public String getPayloadEscrita() { return payloadEscrita; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
