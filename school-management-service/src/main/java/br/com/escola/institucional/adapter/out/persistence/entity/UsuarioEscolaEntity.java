package br.com.escola.institucional.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuario_escola")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioEscolaEntity {

    @Id
    @Column(name = "id_usuario_escola", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_escola", nullable = false)
    private EscolaEntity escola;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UsuarioEscolaEntity(UsuarioEntity usuario, EscolaEntity escola) {
        this.usuario = usuario;
        this.escola = escola;
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
