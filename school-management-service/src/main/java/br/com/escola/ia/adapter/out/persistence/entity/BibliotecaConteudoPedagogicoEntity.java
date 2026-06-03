package br.com.escola.ia.adapter.out.persistence.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import br.com.escola.catalogo.adapter.out.persistence.entity.DisciplinaEntity;
import br.com.escola.professor.adapter.out.persistence.entity.ProfessorEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "biblioteca_conteudo_pedagogico")
public class BibliotecaConteudoPedagogicoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_biblioteca_conteudo_pedagogico", nullable = false)
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 180)
    private String titulo;

    @Column(name = "tema", length = 180)
    private String tema;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "origem", nullable = false, length = 40)
    private String origem;

    @Column(name = "reutilizavel", nullable = false)
    private Boolean reutilizavel;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_disciplina", referencedColumnName = "id_disciplina")
    private DisciplinaEntity disciplina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_professor", referencedColumnName = "id_professor")
    private ProfessorEntity professor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_conteudo_ia", referencedColumnName = "id_tipo_conteudo_ia")
    private TipoConteudoIAEntity tipoConteudoIA;
}
