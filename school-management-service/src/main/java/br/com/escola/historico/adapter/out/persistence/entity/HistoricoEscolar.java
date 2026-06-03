package br.com.escola.historico.adapter.out.persistence.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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

    @Transient
    private String nomeAluno;

    @Transient
    private String rgRen;

    @Transient
    private String ra;

    @Transient
    private String rm;

    @Transient
    private LocalDate dataNascimento;

    @Transient
    private String municipioNascimento;

    @Transient
    private String estadoNascimento;

    @Transient
    private String paisNascimento;

    @Transient
    private String nomeEscola;

    @Transient
    private String enderecoEscola;

    @Transient
    private String municipioEscola;

    @Transient
    private String cepEscola;

    @Transient
    private String telefoneEscola;

    @Transient
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "historicoEscolar",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<HistoricoEscolarItem> componentesCurriculares = new ArrayList<>();

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

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
