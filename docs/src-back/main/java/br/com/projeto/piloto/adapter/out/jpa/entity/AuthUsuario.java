package br.com.projeto.piloto.adapter.out.jpa.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "AUTH_USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUsuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DS_LOGIN", nullable = false, unique = true)
    private String login;

    @Column(name = "DS_SENHA", nullable = false)
    private String senha;

    @Column(name = "NM_USUARIO", nullable = false)
    private String nome;

    @Column(name = "FL_ATIVO", nullable = false)
    private String ativo; 

    @Column(name = "DS_EMAIL")
    private String email;

    @Column(name = "CRIADO_EM")
    private LocalDateTime criadoEm;

    @Column(name = "ATUALIZADO_EM")
    private LocalDateTime atualizadoEm;
    
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.criadoEm = now;
        this.atualizadoEm = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "AUTH_USUARIO_PERFIL",
        joinColumns = @JoinColumn(name = "AUTH_USUARIO_ID"),
        inverseJoinColumns = @JoinColumn(name = "AUTH_PERFIL_ID")
    )
    @Builder.Default
    private Set<AuthPerfil> perfis = new HashSet<>();


}
