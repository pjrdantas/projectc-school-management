package br.com.escola.matricula.adapter.in.web;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.matricula.adapter.in.web.dto.MatriculaAcademicoResumoResponse;
import br.com.escola.matricula.application.service.MatriculaAcademicoResumoService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/matriculas/{matriculaId}/academico")
public class MatriculaAcademicoController {

    private final MatriculaAcademicoResumoService resumoService;

    public MatriculaAcademicoController(MatriculaAcademicoResumoService resumoService) {
        this.resumoService = resumoService;
    }

    @GetMapping
    @Operation(summary = "Consulta resumo acadêmico da matrícula")
    public MatriculaAcademicoResumoResponse consultar(@PathVariable @NonNull UUID matriculaId) {
        return resumoService.consultar(matriculaId);
    }
}
