package br.com.escola.matricula.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.matricula.adapter.out.persistence.entity.StatusEtapaMatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.StatusMatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.entity.TipoMatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.StatusEtapaMatriculaJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.StatusMatriculaJpaRepository;
import br.com.escola.matricula.adapter.out.persistence.repository.TipoMatriculaJpaRepository;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/matriculas/catalogos")
public class MatriculaCatalogoController {

    private final TipoMatriculaJpaRepository tipoMatriculaJpaRepository;
    private final StatusMatriculaJpaRepository statusMatriculaJpaRepository;
    private final StatusEtapaMatriculaJpaRepository statusEtapaMatriculaJpaRepository;

    public MatriculaCatalogoController(
            TipoMatriculaJpaRepository tipoMatriculaJpaRepository,
            StatusMatriculaJpaRepository statusMatriculaJpaRepository,
            StatusEtapaMatriculaJpaRepository statusEtapaMatriculaJpaRepository) {
        this.tipoMatriculaJpaRepository = tipoMatriculaJpaRepository;
        this.statusMatriculaJpaRepository = statusMatriculaJpaRepository;
        this.statusEtapaMatriculaJpaRepository = statusEtapaMatriculaJpaRepository;
    }

    @GetMapping("/tipos")
    @Operation(summary = "Lista tipos de matrícula")
    public List<CatalogoMatriculaResponse> listarTipos() {
        return tipoMatriculaJpaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/status")
    @Operation(summary = "Lista status de matrícula")
    public List<CatalogoMatriculaResponse> listarStatus() {
        return statusMatriculaJpaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/status-etapas")
    @Operation(summary = "Lista status de etapas de matrícula")
    public List<CatalogoMatriculaResponse> listarStatusEtapas() {
        return statusEtapaMatriculaJpaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private CatalogoMatriculaResponse toResponse(TipoMatriculaEntity entity) {
        return new CatalogoMatriculaResponse(entity.getId(), entity.getCodigo(), entity.getDescricao());
    }

    private CatalogoMatriculaResponse toResponse(StatusMatriculaEntity entity) {
        return new CatalogoMatriculaResponse(entity.getId(), entity.getCodigo(), entity.getDescricao());
    }

    private CatalogoMatriculaResponse toResponse(StatusEtapaMatriculaEntity entity) {
        return new CatalogoMatriculaResponse(entity.getId(), entity.getCodigo(), entity.getDescricao());
    }
}
