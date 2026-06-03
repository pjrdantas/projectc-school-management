package br.com.escola.catalogo.adapter.in.web.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.catalogo.adapter.in.web.dto.CatalogoAcademicoResponse;
import br.com.escola.catalogo.adapter.out.persistence.entity.NivelEnsinoEntity;
import br.com.escola.catalogo.adapter.out.persistence.entity.TurnoEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.NivelEnsinoJpaRepository;
import br.com.escola.catalogo.adapter.out.persistence.repository.TurnoJpaRepository;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/academico/catalogos")
public class AcademicoCatalogoController {

    private final NivelEnsinoJpaRepository nivelEnsinoJpaRepository;
    private final TurnoJpaRepository turnoJpaRepository;

    public AcademicoCatalogoController(
            NivelEnsinoJpaRepository nivelEnsinoJpaRepository,
            TurnoJpaRepository turnoJpaRepository) {
        this.nivelEnsinoJpaRepository = nivelEnsinoJpaRepository;
        this.turnoJpaRepository = turnoJpaRepository;
    }

    @GetMapping("/niveis-ensino")
    @Operation(summary = "Lista níveis de ensino")
    public List<CatalogoAcademicoResponse> listarNiveisEnsino() {
        return nivelEnsinoJpaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/turnos")
    @Operation(summary = "Lista turnos")
    public List<CatalogoAcademicoResponse> listarTurnos() {
        return turnoJpaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private CatalogoAcademicoResponse toResponse(NivelEnsinoEntity entity) {
        return new CatalogoAcademicoResponse(entity.getId(), entity.getCodigo(), entity.getDescricao());
    }

    private CatalogoAcademicoResponse toResponse(TurnoEntity entity) {
        return new CatalogoAcademicoResponse(entity.getId(), entity.getCodigo(), entity.getDescricao());
    }
}
