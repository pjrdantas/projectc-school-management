package br.com.escola.accesscontrol.adapter.out.persistence.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "perfil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilEntity {

    @Id
    @Column(name = "id_perfil", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "descricao", length = 255)
    private String descricao;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "perfis")
    @Builder.Default
    private Set<UsuarioEntity> usuarios = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "perfil_permissao",
            joinColumns = @jakarta.persistence.JoinColumn(name = "id_perfil"),
            inverseJoinColumns = @jakarta.persistence.JoinColumn(name = "id_permissao")
    )
    @Builder.Default
    private Set<PermissaoEntity> permissoes = new HashSet<>();

   
    
   
}
