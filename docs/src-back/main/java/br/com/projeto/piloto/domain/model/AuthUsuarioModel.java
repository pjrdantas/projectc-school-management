package br.com.projeto.piloto.domain.model;

import java.time.LocalDateTime;
import java.util.Set;

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

    private Long id;
    private String login;
    private String senha;
    private String nome;
    private String ativo;
    private String email;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    @Builder.Default
    private Set<AuthPerfilModel> perfis = Set.of();

    public boolean isAtivo() {
        return "S".equalsIgnoreCase(this.ativo);
    }

}
