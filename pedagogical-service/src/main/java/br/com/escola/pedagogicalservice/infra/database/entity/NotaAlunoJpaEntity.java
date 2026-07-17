package br.com.escola.pedagogicalservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_grade_record")
public class NotaAlunoJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "assessment_id", nullable = false)
    private UUID avaliacaoId;

    @Column(name = "enrollment_id")
    private UUID matriculaId;

    @Lob
    @Column(name = "payload_json", nullable = false)
    private String payloadJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected NotaAlunoJpaEntity() {
    }

    public NotaAlunoJpaEntity(UUID id, UUID schoolId, UUID avaliacaoId, UUID matriculaId, String payloadJson, LocalDateTime createdAt) {
        this.id = id;
        this.schoolId = schoolId;
        this.avaliacaoId = avaliacaoId;
        this.matriculaId = matriculaId;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getAvaliacaoId() { return avaliacaoId; }
    public UUID getMatriculaId() { return matriculaId; }
    public String getPayloadJson() { return payloadJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
