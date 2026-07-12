package br.com.escola.historico.adapter.out.persistence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "historico_escolar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoEscolar {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_historico_escolar")
    private UUID id;

    @Column(name = "id_aluno")
    private UUID alunoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_aluno", insertable = false, updatable = false)
    private AlunoEntity aluno;

    @Column(name = "origem", nullable = false, length = 20)
    private String origem;

    @Column(name = "nome_aluno", nullable = false, length = 255)
    private String nomeAluno;

    @Column(name = "rg_ren", length = 80)
    private String rgRen;

    @Column(name = "ra", length = 80)
    private String ra;

    @Column(name = "rm", length = 80)
    private String rm;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "municipio_nascimento", length = 150)
    private String municipioNascimento;

    @Column(name = "estado_nascimento", length = 2)
    private String estadoNascimento;

    @Column(name = "pais_nascimento", length = 100)
    private String paisNascimento;

    @Column(name = "nome_escola", length = 255)
    private String nomeEscola;

    @Column(name = "endereco_escola", length = 255)
    private String enderecoEscola;

    @Column(name = "municipio_escola", length = 150)
    private String municipioEscola;

    @Column(name = "cep_escola", length = 10)
    private String cepEscola;

    @Column(name = "telefone_escola", length = 30)
    private String telefoneEscola;

    @Column(name = "email_escola", length = 150)
    private String emailEscola;

    @Column(name = "ano_conclusao")
    private Integer anoConclusao;

    @Column(name = "ensino_concluido")
    private String ensinoConcluido;

    @Column(name = "data_emissao")
    private LocalDate dataEmissao;

    @Column(name = "diretor_nome")
    private String diretorNome;

    @Column(name = "diretor_rg")
    private String diretorRg;

    @Column(name = "gerente_organizacao_nome")
    private String gerenteOrganizacaoNome;

    @Column(name = "gerente_organizacao_rg")
    private String gerenteOrganizacaoRg;

    @Column(name = "doe_numero")
    private String doeNumero;

    @Column(name = "doe_data")
    private LocalDate doeData;

    @Column(name = "doe_volume")
    private String doeVolume;

    @Column(name = "doe_pagina")
    private String doePagina;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "id_matricula")
    private UUID matriculaId;

    @Column(name = "id_transferencia_aluno")
    private UUID transferenciaAlunoId;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "bloqueado", nullable = false)
    private Boolean bloqueado;

    @Column(name = "serie_matricula_atual")
    private Integer serieMatriculaAtual;

    @Column(name = "serie_concluida_origem")
    private Integer serieConcluidaOrigem;

    @Column(name = "escola_origem_nome", length = 150)
    private String escolaOrigemNome;

    @Column(name = "data_transferencia")
    private LocalDate dataTransferencia;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @OneToMany(
            mappedBy = "historicoEscolar",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<HistoricoEscolarItem> componentesCurriculares = new ArrayList<>();

    @OneToMany(
            mappedBy = "historicoEscolar",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<HistoricoEscolarPendencia> pendencias = new ArrayList<>();

    public void addComponenteCurricular(HistoricoEscolarItem item) {
        item.setHistoricoEscolar(this);
        componentesCurriculares.add(item);
    }

    public void replaceComponentesCurriculares(List<HistoricoEscolarItem> itens) {
        componentesCurriculares.clear();
        if (itens == null) {
            return;
        }
        itens.forEach(this::addComponenteCurricular);
    }

    public void addPendencia(HistoricoEscolarPendencia pendencia) {
        pendencia.setHistoricoEscolar(this);
        pendencias.add(pendencia);
    }

    public void replacePendencias(List<HistoricoEscolarPendencia> novasPendencias) {
        pendencias.clear();
        if (novasPendencias == null) {
            return;
        }
        novasPendencias.forEach(this::addPendencia);
    }

    @PrePersist
    public void prePersist() {
        if (origem == null || origem.isBlank()) {
            origem = "EXTERNO";
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "RASCUNHO";
        }
        if (bloqueado == null) {
            bloqueado = false;
        }
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        if (status == null || status.isBlank()) {
            status = "RASCUNHO";
        }
        if (bloqueado == null) {
            bloqueado = false;
        }
        atualizadoEm = LocalDateTime.now();
    }
}
