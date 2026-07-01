package br.com.escola.professor.adapter.in.web;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.professor.adapter.in.web.dto.DiarioClasseResponse;
import br.com.escola.professor.application.service.DiarioClasseConsultaService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/diarios-classe")
public class DiarioClasseController {

    private final DiarioClasseConsultaService diarioClasseConsultaService;

    public DiarioClasseController(DiarioClasseConsultaService diarioClasseConsultaService) {
        this.diarioClasseConsultaService = diarioClasseConsultaService;
    }

    @GetMapping
    @Operation(summary = "Carrega o diário de classe mensal consolidado")
    public DiarioClasseResponse carregar(
            @RequestParam UUID idProfessor,
            @RequestParam UUID idTurma,
            @RequestParam UUID idDisciplina,
            @RequestParam Integer anoLetivo,
            @RequestParam Integer mes,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        return diarioClasseConsultaService.carregar(
                idProfessor,
                idTurma,
                idDisciplina,
                anoLetivo,
                mes,
                dataReferencia);
    }
}
