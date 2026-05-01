package br.com.projeto.piloto.adapter.out.jpa.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "AUTH_PERFIL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthPerfil implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NM_PERFIL", nullable = false)
    private String nmPerfil;

    @ManyToMany(mappedBy = "perfis")
    @Builder.Default
    private Set<AuthUsuario> usuarios = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "AUTH_PERFIL_PERMISSAO",
        joinColumns = @JoinColumn(name = "AUTH_PERFIL_ID"),
        inverseJoinColumns = @JoinColumn(name = "AUTH_PERMISSAO_ID")
    )
    @Builder.Default
    private Set<AuthPermissao> permissoes = new HashSet<>();
}