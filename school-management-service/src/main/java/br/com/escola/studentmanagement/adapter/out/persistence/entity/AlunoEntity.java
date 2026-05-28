package br.com.escola.studentmanagement.adapter.out.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import br.com.escola.shared.person.entity.PessoaEntity;

@Entity
@Table(name = "aluno")
public class AlunoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_aluno")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_pessoa", nullable = false)
    private PessoaEntity pessoa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_status_aluno")
    private StatusAlunoEntity statusAluno;

    @Column(name = "ra", length = 80)
    private String ra;

    @Column(name = "rm", length = 80)
    private String rm;

    @Column(name = "emancipado", nullable = false)
    private boolean emancipado = false;

    @Column(name = "data_ingresso")
    private LocalDate dataIngresso;

    @Column(name = "data_saida")
    private LocalDate dataSaida;

    @Column(name = "motivo_saida", columnDefinition = "TEXT")
    private String motivoSaida;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public PessoaEntity getPessoa() { return pessoa; }
    public void setPessoa(PessoaEntity pessoa) { this.pessoa = pessoa; }
    public StatusAlunoEntity getStatusAlunoEntity() { return statusAluno; }
    public void setStatusAluno(StatusAlunoEntity statusAluno) { this.statusAluno = statusAluno; }
    public String getRa() { return ra; }
    public void setRa(String ra) { this.ra = ra; }
    public String getRm() { return rm; }
    public void setRm(String rm) { this.rm = rm; }
    public boolean isEmancipado() { return emancipado; }
    public void setEmancipado(boolean emancipado) { this.emancipado = emancipado; }
    public LocalDate getDataIngresso() { return dataIngresso; }
    public void setDataIngresso(LocalDate dataIngresso) { this.dataIngresso = dataIngresso; }
    public LocalDate getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDate dataSaida) { this.dataSaida = dataSaida; }
    public String getMotivoSaida() { return motivoSaida; }
    public void setMotivoSaida(String motivoSaida) { this.motivoSaida = motivoSaida; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getNomeCompleto() {
        return pessoa != null ? pessoa.getNomeCompleto() : null;
    }

    public void setNomeCompleto(String nomeCompleto) {
        if (pessoa != null) pessoa.setNomeCompleto(nomeCompleto);
    }

    public String getCpf() {
        return pessoa != null ? pessoa.getCpf() : null;
    }

    public void setCpf(String cpf) {
        if (pessoa != null) pessoa.setCpf(cpf);
    }

    public String getEmail() {
        return pessoa != null ? pessoa.getEmail() : null;
    }

    public void setEmail(String email) {
        if (pessoa != null) pessoa.setEmail(email);
    }

    public String getTelefone() {
        return pessoa != null ? pessoa.getTelefone() : null;
    }

    public void setTelefone(String telefone) {
        if (pessoa != null) pessoa.setTelefone(telefone);
    }

    public LocalDate getDataNascimento() {
        return pessoa != null ? pessoa.getDataNascimento() : null;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        if (pessoa != null) pessoa.setDataNascimento(dataNascimento);
    }

    public String getRg() {
        return pessoa != null ? pessoa.getRg() : null;
    }

    public void setRg(String rg) {
        if (pessoa != null) pessoa.setRg(rg);
    }

    public String getOrgaoEmissorRg() {
        return pessoa != null ? pessoa.getOrgaoEmissorRg() : null;
    }

    public void setOrgaoEmissorRg(String orgaoEmissorRg) {
        if (pessoa != null) pessoa.setOrgaoEmissorRg(orgaoEmissorRg);
    }

    public String getUfRg() {
        return pessoa != null ? pessoa.getUfRg() : null;
    }

    public void setUfRg(String ufRg) {
        if (pessoa != null) pessoa.setUfRg(ufRg);
    }

    public String getNacionalidade() {
        return pessoa != null ? pessoa.getNacionalidade() : null;
    }

    public void setNacionalidade(String nacionalidade) {
        if (pessoa != null) pessoa.setNacionalidade(nacionalidade);
    }

    public String getNaturalidade() {
        return pessoa != null ? pessoa.getNaturalidade() : null;
    }

    public void setNaturalidade(String naturalidade) {
        if (pessoa != null) pessoa.setNaturalidade(naturalidade);
    }

    public String getSexo() {
        return pessoa != null ? pessoa.getSexo() : null;
    }

    public void setSexo(String sexo) {
        if (pessoa != null) pessoa.setSexo(sexo);
    }

    public String getNomeSocial() {
        return pessoa != null ? pessoa.getNomeSocial() : null;
    }

    public void setNomeSocial(String nomeSocial) {
        if (pessoa != null) pessoa.setNomeSocial(nomeSocial);
    }

    public String getCep() {
        return null;
    }

    public void setCep(String cep) {
    }

    public String getLogradouro() {
        return null;
    }

    public void setLogradouro(String logradouro) {
    }

    public String getNumero() {
        return null;
    }

    public void setNumero(String numero) {
    }

    public String getComplemento() {
        return null;
    }

    public void setComplemento(String complemento) {
    }

    public String getBairro() {
        return null;
    }

    public void setBairro(String bairro) {
    }

    public String getCidade() {
        return null;
    }

    public void setCidade(String cidade) {
    }

    public String getUf() {
        return null;
    }

    public void setUf(String uf) {
    }

    public String getStatusAluno() {
        return statusAluno != null ? statusAluno.getCodigo() : null;
    }


    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
