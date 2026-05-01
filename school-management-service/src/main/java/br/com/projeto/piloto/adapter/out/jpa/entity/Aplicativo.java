package br.com.projeto.piloto.adapter.out.jpa.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "APLICATIVOS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aplicativo implements Serializable {
    private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(name="NM_APLICATIVO")
	private String nmAplicativo;
    
	@Column(name="DS_APLICATIVO")
	private String dsAplicativo;

	@Column(name="DS_URL")
	private String dsUrl;

	@Column(name="NM_MODULO")
	private String nmModulo;
	
	@Column(name="MODULO_EXPOSTO")
	private String moduloExposto;

	@Column(name="DS_ROTA")
	private String dsRota;

	@Column(name="FL_ATIVO")
	private String flAtivo;

    @Column(name = "CRIADO_EM", updatable = false)
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
}
