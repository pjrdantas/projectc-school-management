package br.com.escola.accesscontrol.domain.model;

import java.time.LocalDateTime;
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
public class PermissaoModel {

    private UUID id;
    private String codigo;
    private String descricao;
    private LocalDateTime createdAt;

}