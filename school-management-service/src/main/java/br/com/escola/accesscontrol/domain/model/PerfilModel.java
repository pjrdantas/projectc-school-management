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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilModel {

    private UUID id;
    private String codigo;
    private String nome;
    private String descricao;
    private LocalDateTime createdAt;

    @Builder.Default
    private Set<PermissaoModel> permissoes = new HashSet<>();
}