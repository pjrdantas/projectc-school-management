package br.com.escola.dashboard.adapter.out.persistence.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "publico_dashboard")
public class PublicoDashboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_publico_dashboard", nullable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(name = "descricao", nullable = false, length = 120)
    private String descricao;
}
