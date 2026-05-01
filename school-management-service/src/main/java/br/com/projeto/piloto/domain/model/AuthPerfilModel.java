package br.com.projeto.piloto.domain.model;

import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor   
@AllArgsConstructor  
public class AuthPerfilModel {
    private Long id;
    private String nmPerfil;

    @Builder.Default
    private Set<AuthPermissaoModel> permissoes = new HashSet<>(); 
}
