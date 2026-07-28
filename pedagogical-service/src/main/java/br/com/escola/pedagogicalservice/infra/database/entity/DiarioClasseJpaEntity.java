package br.com.escola.pedagogicalservice.infra.database.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "class_diary_record")
public class DiarioClasseJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 120)
    private String id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "professor_id")
    private UUID professorId;

    @Column(name = "class_id")
    private UUID turmaId;

    @Column(name = "subject_id")
    private UUID disciplinaId;

    @Column(name = "school_year")
    private Integer anoLetivo;

    @Column(name = "month_number")
    private Integer mes;

    @Column(name = "reference_date")
    private LocalDate dataReferencia;

    @Column(name = "read_payload_json", columnDefinition = "text")
    private String payloadLeitura;

    @Column(name = "write_payload_json", columnDefinition = "text")
    private String payloadEscrita;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected DiarioClasseJpaEntity() {
    }

    public DiarioClasseJpaEntity(
            String id,
            UUID schoolId,
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia,
            String payloadLeitura,
            String payloadEscrita,
            LocalDateTime updatedAt) {
        this.id = id;
        this.schoolId = schoolId;
        this.professorId = professorId;
        this.turmaId = turmaId;
        this.disciplinaId = disciplinaId;
        this.anoLetivo = anoLetivo;
        this.mes = mes;
        this.dataReferencia = dataReferencia;
        this.payloadLeitura = payloadLeitura;
        this.payloadEscrita = payloadEscrita;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public UUID getProfessorId() { return professorId; }
    public UUID getTurmaId() { return turmaId; }
    public UUID getDisciplinaId() { return disciplinaId; }
    public Integer getAnoLetivo() { return anoLetivo; }
    public Integer getMes() { return mes; }
    public LocalDate getDataReferencia() { return dataReferencia; }
    public String getPayloadLeitura() { return payloadLeitura; }
    public String getPayloadEscrita() { return payloadEscrita; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
