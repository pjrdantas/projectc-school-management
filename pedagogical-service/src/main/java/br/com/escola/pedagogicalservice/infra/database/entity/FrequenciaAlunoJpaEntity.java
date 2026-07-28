package br.com.escola.pedagogicalservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_attendance_record")
public class FrequenciaAlunoJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "lesson_id", nullable = false)
    private UUID aulaId;

    @Column(name = "enrollment_id")
    private UUID matriculaId;

    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected FrequenciaAlunoJpaEntity() {
    }

    public FrequenciaAlunoJpaEntity(UUID id, UUID schoolId, UUID aulaId, UUID matriculaId, String payloadJson, LocalDateTime createdAt) {
        this.id = id;
        this.schoolId = schoolId;
        this.aulaId = aulaId;
        this.matriculaId = matriculaId;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getAulaId() { return aulaId; }
    public UUID getMatriculaId() { return matriculaId; }
    public String getPayloadJson() { return payloadJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
