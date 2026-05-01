package br.com.projeto.piloto.domain.model;

import java.time.LocalDateTime;

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
public class AuthSessaoModel {

    private Long id;
    private Long authUsuarioId;
    private String token;
    private String refreshToken;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataExpiracao;
    private String ativo;

    public boolean isAtiva() {
        return "S".equalsIgnoreCase(this.ativo);
    }

    public boolean isExpirada() {
        return LocalDateTime.now().isAfter(this.dataExpiracao);
    }
}
