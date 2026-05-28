package br.com.escola.responsavelmanagement.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import br.com.escola.shared.person.entity.PessoaEntity;

@Entity
@Table(name = "responsavel")
public class ResponsavelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_responsavel")
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_pessoa", nullable = false)
    private PessoaEntity pessoa;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public PessoaEntity getPessoa() { return pessoa; }
    public void setPessoa(PessoaEntity pessoa) { this.pessoa = pessoa; }

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

    public String getRg() {
        return pessoa != null ? pessoa.getRg() : null;
    }

    public void setRg(String rg) {
        if (pessoa != null) pessoa.setRg(rg);
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
