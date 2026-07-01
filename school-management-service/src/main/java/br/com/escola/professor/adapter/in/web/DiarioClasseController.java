package br.com.escola.professor.adapter.in.web;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.professor.adapter.in.web.dto.DiarioClasseChecagemRequest;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseChecagemResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseSalvarRequest;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseSalvarResponse;
import br.com.escola.professor.adapter.in.web.dto.DiarioClasseResponse;
import br.com.escola.professor.application.dto.internal.DiarioClasseChecagemResumo;
import br.com.escola.professor.application.service.DiarioClasseChecagemService;
import br.com.escola.professor.application.service.DiarioClasseConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/diarios-classe")
public class DiarioClasseController {

    private final DiarioClasseConsultaService diarioClasseConsultaService;
    private final DiarioClasseChecagemService diarioClasseChecagemService;

    public DiarioClasseController(
            DiarioClasseConsultaService diarioClasseConsultaService,
            DiarioClasseChecagemService diarioClasseChecagemService) {
        this.diarioClasseConsultaService = diarioClasseConsultaService;
        this.diarioClasseChecagemService = diarioClasseChecagemService;
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

    @PutMapping("/{idDiarioClasse}")
    @Operation(summary = "Salva o lancamento controlado do diario de classe")
    public DiarioClasseSalvarResponse salvar(
            @PathVariable String idDiarioClasse,
            @Valid @RequestBody DiarioClasseSalvarRequest request) {
        return diarioClasseConsultaService.salvar(idDiarioClasse, request);
    }

    @PostMapping("/lancamentos/{idLancamento}/checagens/coordenacao")
    @Operation(summary = "Checa o lancamento do diario pela coordenacao")
    public ResponseEntity<DiarioClasseChecagemResponse> checarCoordenacao(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable UUID idLancamento,
            @Valid @RequestBody(required = false) DiarioClasseChecagemRequest request) {
        return ResponseEntity.ok(toResponse(diarioClasseChecagemService.checarCoordenacao(
                extrairBearerToken(authorization),
                idLancamento,
                observacao(request))));
    }

    @PostMapping("/lancamentos/{idLancamento}/checagens/direcao")
    @Operation(summary = "Checa o lancamento do diario pela direcao")
    public ResponseEntity<DiarioClasseChecagemResponse> checarDirecao(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable UUID idLancamento,
            @Valid @RequestBody(required = false) DiarioClasseChecagemRequest request) {
        return ResponseEntity.ok(toResponse(diarioClasseChecagemService.checarDirecao(
                extrairBearerToken(authorization),
                idLancamento,
                observacao(request))));
    }

    private DiarioClasseChecagemResponse toResponse(DiarioClasseChecagemResumo resumo) {
        return new DiarioClasseChecagemResponse(
                resumo.diarioClasseLancamentoId(),
                resumo.escolaId(),
                resumo.status(),
                resumo.checadoCoordenacaoPorFuncionario(),
                resumo.checadoCoordenacaoEm(),
                resumo.checadoDirecaoPorFuncionario(),
                resumo.checadoDirecaoEm(),
                resumo.bloqueado());
    }

    private String observacao(DiarioClasseChecagemRequest request) {
        return request == null ? null : request.observacao();
    }

    private String extrairBearerToken(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Bearer token obrigatorio");
        }
        String token = authorization.substring(7).trim();
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Bearer token obrigatorio");
        }
        return token;
    }
}
