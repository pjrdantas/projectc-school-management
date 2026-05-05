package br.com.escola.accesscontrol.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
public class AuthUsuarioModel {

    private UUID id;
    private String username;
    private String nome;
    private String email;
    private String senhaHash;
    private boolean ativo;

    private LocalDateTime createdAt;

    @Builder.Default
    private Set<PerfilModel> perfis = new HashSet<>();

    public boolean isAtivo() {
        return this.ativo;
    }
}