package br.com.escola.dashboard.adapter.out.persistence.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
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
@Table(name = "dashboard_indicador_snapshot")
public class DashboardIndicadorSnapshotEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_dashboard_indicador_snapshot", nullable = false)
    private UUID id;

    @Column(name = "codigo_indicador", nullable = false, length = 100)
    private String codigoIndicador;

    @Column(name = "descricao", nullable = false, length = 180)
    private String descricao;

    @Column(name = "valor_numeric")
    private BigDecimal valorNumeric;

    @Column(name = "valor_texto", length = 180)
    private String valorTexto;

    @Column(name = "referencia_data", nullable = false)
    private LocalDate referenciaData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_publico_dashboard", referencedColumnName = "id_publico_dashboard", nullable = false)
    private PublicoDashboardEntity publicoDashboard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_escola", referencedColumnName = "id_escola", nullable = false)
    private EscolaEntity escola;
}
