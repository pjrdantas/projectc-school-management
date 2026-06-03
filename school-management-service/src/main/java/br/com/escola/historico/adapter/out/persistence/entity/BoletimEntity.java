package br.com.escola.historico.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "boletim")
public class BoletimEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_boletim", nullable = false)
    private UUID id;

    @Column(name = "periodo_referencia", nullable = false, unique = true, length = 60)
    private String periodoReferencia;

    @Column(name = "data_fechamento")
    private LocalDate dataFechamento;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_matricula", referencedColumnName = "id_matricula", nullable = false)
    private MatriculaEntity matricula;
}
