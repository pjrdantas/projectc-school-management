package br.com.escola.planningaiservice.infra.persistence.jpa.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "planejamento_bimestral")
public class PlanejamentoBimestralJpaEntity {

    @Id
    @Column(name = "id_planejamento_bimestral", nullable = false)
    private UUID id;

    @Column(name = "id_escola", nullable = false)
    private UUID escolaId;

    @Column(name = "id_professor_turma_disciplina", nullable = false)
    private UUID professorTurmaDisciplinaId;

    @Column(name = "id_periodo_avaliativo")
    private UUID periodoAvaliativoId;

    @Column(nullable = false, length = 60)
    private String status;

    @Column(nullable = false, length = 180)
    private String titulo;

    @Column(name = "tema_principal", nullable = false, length = 180)
    private String temaPrincipal;

    @Column(name = "descricao_inicial", nullable = false, columnDefinition = "TEXT")
    private String descricaoInicial;

    @Column(name = "objetivo_geral", columnDefinition = "TEXT")
    private String objetivoGeral;

    @Column(name = "observacao_professor", columnDefinition = "TEXT")
    private String observacaoProfessor;

    @Column(name = "conteudo_final_aprovado", columnDefinition = "TEXT")
    private String conteudoFinalAprovado;

    @Column(nullable = false)
    private boolean reutilizavel;

    @Column(name = "criado_com_auxilio_ia", nullable = false)
    private boolean criadoComAuxilioIa;

    @Column(name = "aprovado_pelo_professor", nullable = false)
    private boolean aprovadoPeloProfessor;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PlanejamentoBimestralJpaEntity() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEscolaId() { return escolaId; }
    public void setEscolaId(UUID escolaId) { this.escolaId = escolaId; }
    public UUID getProfessorTurmaDisciplinaId() { return professorTurmaDisciplinaId; }
    public void setProfessorTurmaDisciplinaId(UUID value) { this.professorTurmaDisciplinaId = value; }
    public UUID getPeriodoAvaliativoId() { return periodoAvaliativoId; }
    public void setPeriodoAvaliativoId(UUID value) { this.periodoAvaliativoId = value; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getTemaPrincipal() { return temaPrincipal; }
    public void setTemaPrincipal(String value) { this.temaPrincipal = value; }
    public String getDescricaoInicial() { return descricaoInicial; }
    public void setDescricaoInicial(String value) { this.descricaoInicial = value; }
    public String getObjetivoGeral() { return objetivoGeral; }
    public void setObjetivoGeral(String value) { this.objetivoGeral = value; }
    public String getObservacaoProfessor() { return observacaoProfessor; }
    public void setObservacaoProfessor(String value) { this.observacaoProfessor = value; }
    public String getConteudoFinalAprovado() { return conteudoFinalAprovado; }
    public void setConteudoFinalAprovado(String value) { this.conteudoFinalAprovado = value; }
    public boolean isReutilizavel() { return reutilizavel; }
    public void setReutilizavel(boolean value) { this.reutilizavel = value; }
    public boolean isCriadoComAuxilioIa() { return criadoComAuxilioIa; }
    public void setCriadoComAuxilioIa(boolean value) { this.criadoComAuxilioIa = value; }
    public boolean isAprovadoPeloProfessor() { return aprovadoPeloProfessor; }
    public void setAprovadoPeloProfessor(boolean value) { this.aprovadoPeloProfessor = value; }
    public LocalDateTime getDataAprovacao() { return dataAprovacao; }
    public void setDataAprovacao(LocalDateTime value) { this.dataAprovacao = value; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { this.createdAt = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { this.updatedAt = value; }
}
