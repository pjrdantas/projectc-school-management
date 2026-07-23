package br.com.escola.enrollmentdocumentservice.infra.database.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "source_school")
public class EscolaOrigemJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "school_id", nullable = false)
    private UUID schoolId;

    @Column(name = "name", nullable = false, length = 180)
    private String nomeEscola;

    @Column(name = "inep_code", length = 32)
    private String codigoInep;

    @Column(name = "cnpj", length = 32)
    private String cnpj;

    @Column(name = "cep", length = 16)
    private String cep;

    @Column(name = "street", length = 180)
    private String logradouro;

    @Column(name = "address_number", length = 32)
    private String numero;

    @Column(name = "address_extra", length = 120)
    private String complemento;

    @Column(name = "district", length = 120)
    private String bairro;

    @Column(name = "city", length = 120)
    private String cidade;

    @Column(name = "state_code", length = 8)
    private String uf;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected EscolaOrigemJpaEntity() {
    }

    public EscolaOrigemJpaEntity(
            UUID id,
            UUID schoolId,
            String nomeEscola,
            String codigoInep,
            String cnpj,
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf,
            LocalDateTime createdAt) {
        this.id = id;
        this.schoolId = schoolId;
        this.nomeEscola = nomeEscola;
        this.codigoInep = codigoInep;
        this.cnpj = cnpj;
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getSchoolId() { return schoolId; }
    public String getNomeEscola() { return nomeEscola; }
    public String getCodigoInep() { return codigoInep; }
    public String getCnpj() { return cnpj; }
    public String getCep() { return cep; }
    public String getLogradouro() { return logradouro; }
    public String getNumero() { return numero; }
    public String getComplemento() { return complemento; }
    public String getBairro() { return bairro; }
    public String getCidade() { return cidade; }
    public String getUf() { return uf; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
