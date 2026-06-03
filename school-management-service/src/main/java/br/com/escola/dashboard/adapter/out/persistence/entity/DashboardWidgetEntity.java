package br.com.escola.dashboard.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
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
@Table(name = "dashboard_widget")
public class DashboardWidgetEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_dashboard_widget", nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, length = 80)
    private String codigo;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "tipo_widget", nullable = false, length = 40)
    private String tipoWidget;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    @Column(name = "query_referencia", length = 150)
    private String queryReferencia;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dashboard", referencedColumnName = "id_dashboard", nullable = false)
    private DashboardEntity dashboard;
}
