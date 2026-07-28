package br.com.escola.pedagogicalservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "assessment_record")
public class AvaliacaoJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "allocation_id")
    private UUID professorTurmaDisciplinaId;

    @Column(name = "class_id")
    private UUID turmaId;

    @Column(name = "title", length = 180)
    private String titulo;

    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected AvaliacaoJpaEntity() {
    }

    public AvaliacaoJpaEntity(
            UUID id,
            UUID schoolId,
            UUID professorTurmaDisciplinaId,
            UUID turmaId,
            String titulo,
            String payloadJson,
            LocalDateTime createdAt) {
        this.id = id;
        this.schoolId = schoolId;
        this.professorTurmaDisciplinaId = professorTurmaDisciplinaId;
        this.turmaId = turmaId;
        this.titulo = titulo;
        this.payloadJson = payloadJson;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getProfessorTurmaDisciplinaId() { return professorTurmaDisciplinaId; }
    public UUID getTurmaId() { return turmaId; }
    public String getTitulo() { return titulo; }
    public String getPayloadJson() { return payloadJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
