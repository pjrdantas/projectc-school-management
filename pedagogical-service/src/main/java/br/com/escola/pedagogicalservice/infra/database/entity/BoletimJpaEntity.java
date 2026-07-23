package br.com.escola.pedagogicalservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "report_card_record")
public class BoletimJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "enrollment_id", nullable = false)
    private UUID matriculaId;

    @Column(name = "is_closure", nullable = false)
    private boolean fechamento;

    @Lob
    @Column(name = "payload_json", nullable = false)
    private String payloadJson;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BoletimJpaEntity() {
    }

    public BoletimJpaEntity(UUID id, UUID schoolId, UUID matriculaId, boolean fechamento, String payloadJson, LocalDateTime updatedAt) {
        this.id = id;
        this.schoolId = schoolId;
        this.matriculaId = matriculaId;
        this.fechamento = fechamento;
        this.payloadJson = payloadJson;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getMatriculaId() { return matriculaId; }
    public boolean isFechamento() { return fechamento; }
    public String getPayloadJson() { return payloadJson; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
