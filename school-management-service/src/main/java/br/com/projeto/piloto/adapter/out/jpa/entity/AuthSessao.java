package br.com.projeto.piloto.adapter.out.jpa.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "AUTH_SESSAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthSessao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "AUTH_USUARIO_ID", nullable = false)
    private Long authUsuarioId;

    @Column(name = "DS_TOKEN", nullable = false, length = 1000)
    private String token;

    @Column(name = "DS_REFRESH_TOKEN", nullable = false, length = 1000)
    private String refreshToken;

    @Column(name = "DATA_CRIACAO", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "DATA_EXPIRACAO", nullable = false)
    private LocalDateTime dataExpiracao;

    @Column(name = "FL_ATIVO", nullable = false)
    private String ativo;

    public boolean isAtiva() {
        return "S".equalsIgnoreCase(this.ativo);
    }

    public boolean isExpirada() {
        return LocalDateTime.now().isAfter(this.dataExpiracao);
    }
}
