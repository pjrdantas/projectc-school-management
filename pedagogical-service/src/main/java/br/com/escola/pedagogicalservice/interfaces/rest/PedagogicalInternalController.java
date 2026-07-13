package br.com.escola.pedagogicalservice.interfaces.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.pedagogicalservice.application.context.InternalHeaders;
import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.AvaliacaoResponse;
import br.com.escola.pedagogicalservice.application.dto.AulaResponse;
import br.com.escola.pedagogicalservice.application.dto.BoletimResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaAlunoResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaProfessorResponse;
import br.com.escola.pedagogicalservice.application.dto.HistoricoEscolarTelaResponse;
import br.com.escola.pedagogicalservice.application.dto.NotaAlunoResponse;
import br.com.escola.pedagogicalservice.application.port.in.AvaliacaoUseCase;
import br.com.escola.pedagogicalservice.application.port.in.AulaUseCase;
import br.com.escola.pedagogicalservice.application.port.in.HistoricoEscolarReadUseCase;
import br.com.escola.pedagogicalservice.application.port.in.HistoricoEscolarWriteUseCase;
import br.com.escola.pedagogicalservice.application.port.in.BoletimQueryUseCase;
import br.com.escola.pedagogicalservice.application.port.in.DiarioClasseReadUseCase;
import br.com.escola.pedagogicalservice.application.port.in.DiarioClasseWriteUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PedagogicalInternalController {

    private final BoletimQueryUseCase boletimQueryUseCase;
    private final AvaliacaoUseCase avaliacaoUseCase;
    private final AulaUseCase aulaUseCase;
    private final DiarioClasseReadUseCase diarioClasseReadUseCase;
    private final DiarioClasseWriteUseCase diarioClasseWriteUseCase;
    private final HistoricoEscolarReadUseCase historicoEscolarReadUseCase;
    private final HistoricoEscolarWriteUseCase historicoEscolarWriteUseCase;

    public PedagogicalInternalController(
            BoletimQueryUseCase boletimQueryUseCase,
            AvaliacaoUseCase avaliacaoUseCase,
            AulaUseCase aulaUseCase,
            DiarioClasseReadUseCase diarioClasseReadUseCase,
            DiarioClasseWriteUseCase diarioClasseWriteUseCase,
            HistoricoEscolarReadUseCase historicoEscolarReadUseCase,
            HistoricoEscolarWriteUseCase historicoEscolarWriteUseCase) {
        this.boletimQueryUseCase = boletimQueryUseCase;
        this.avaliacaoUseCase = avaliacaoUseCase;
        this.aulaUseCase = aulaUseCase;
        this.diarioClasseReadUseCase = diarioClasseReadUseCase;
        this.diarioClasseWriteUseCase = diarioClasseWriteUseCase;
        this.historicoEscolarReadUseCase = historicoEscolarReadUseCase;
        this.historicoEscolarWriteUseCase = historicoEscolarWriteUseCase;
    }

    @GetMapping("/matriculas/{matriculaId}/boletim")
    public BoletimResponse consultarBoletimPorMatricula(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID matriculaId) {
        return boletimQueryUseCase.consultarBoletimPorMatricula(authorization, context, matriculaId);
    }

    @GetMapping("/matriculas/{matriculaId}/boletim/fechamentos")
    public List<BoletimResponse> listarFechamentosPorMatricula(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID matriculaId) {
        return boletimQueryUseCase.listarFechamentosPorMatricula(authorization, context, matriculaId);
    }

    @PostMapping("/aulas")
    @ResponseStatus(HttpStatus.CREATED)
    public AulaResponse criarAula(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestBody String requestBody) {
        return aulaUseCase.criar(authorization, context, requestBody);
    }

    @PostMapping("/avaliacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliacaoResponse criarAvaliacao(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestBody String requestBody) {
        return avaliacaoUseCase.criar(authorization, context, requestBody);
    }

    @GetMapping("/avaliacoes")
    public List<AvaliacaoResponse> listarAvaliacoes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam(required = false) UUID professorTurmaDisciplinaId,
            @RequestParam(required = false) UUID turmaId) {
        return avaliacaoUseCase.listar(authorization, context, professorTurmaDisciplinaId, turmaId);
    }

    @GetMapping("/avaliacoes/{id}")
    public AvaliacaoResponse buscarAvaliacaoPorId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return avaliacaoUseCase.buscarPorId(authorization, context, id);
    }

    @PostMapping("/avaliacoes/{id}/notas")
    @ResponseStatus(HttpStatus.CREATED)
    public NotaAlunoResponse lancarNota(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id,
            @RequestBody String requestBody) {
        return avaliacaoUseCase.lancarNota(authorization, context, id, requestBody);
    }

    @GetMapping("/avaliacoes/{id}/notas")
    public List<NotaAlunoResponse> listarNotasPorAvaliacao(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return avaliacaoUseCase.listarNotasPorAvaliacao(authorization, context, id);
    }

    @GetMapping("/matriculas/{matriculaId}/notas")
    public List<NotaAlunoResponse> listarNotasPorMatricula(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID matriculaId) {
        return avaliacaoUseCase.listarNotasPorMatricula(authorization, context, matriculaId);
    }

    @GetMapping("/aulas")
    public List<AulaResponse> listarAulas(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam(required = false) UUID professorTurmaDisciplinaId,
            @RequestParam(required = false) UUID turmaId) {
        return aulaUseCase.listar(authorization, context, professorTurmaDisciplinaId, turmaId);
    }

    @GetMapping("/aulas/{id}")
    public AulaResponse buscarAulaPorId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return aulaUseCase.buscarPorId(authorization, context, id);
    }

    @PostMapping("/aulas/{id}/frequencia-professor")
    @ResponseStatus(HttpStatus.CREATED)
    public FrequenciaProfessorResponse registrarFrequenciaProfessor(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id,
            @RequestBody String requestBody) {
        return aulaUseCase.registrarFrequenciaProfessor(authorization, context, id, requestBody);
    }

    @GetMapping("/aulas/{id}/frequencia-professor")
    public List<FrequenciaProfessorResponse> listarFrequenciaProfessor(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return aulaUseCase.listarFrequenciaProfessor(authorization, context, id);
    }

    @PostMapping("/aulas/{id}/frequencias-alunos")
    @ResponseStatus(HttpStatus.CREATED)
    public FrequenciaAlunoResponse registrarFrequenciaAluno(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id,
            @RequestBody String requestBody) {
        return aulaUseCase.registrarFrequenciaAluno(authorization, context, id, requestBody);
    }

    @GetMapping("/aulas/{id}/frequencias-alunos")
    public List<FrequenciaAlunoResponse> listarFrequenciasAlunos(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return aulaUseCase.listarFrequenciasAlunos(authorization, context, id);
    }

    @GetMapping("/diarios-classe")
    public ResponseEntity<String> carregarDiarioClasse(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam UUID idProfessor,
            @RequestParam UUID idTurma,
            @RequestParam UUID idDisciplina,
            @RequestParam Integer anoLetivo,
            @RequestParam Integer mes,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        return diarioClasseReadUseCase.carregar(
                authorization,
                context,
                idProfessor,
                idTurma,
                idDisciplina,
                anoLetivo,
                mes,
                dataReferencia);
    }

    @PutMapping("/diarios-classe/{idDiarioClasse}")
    public ResponseEntity<String> salvarDiarioClasse(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable String idDiarioClasse,
            @RequestBody String requestBody) {
        return diarioClasseWriteUseCase.salvar(authorization, context, idDiarioClasse, requestBody);
    }

    @PostMapping("/historicos-escolares")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<String> criarHistoricoEscolar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestBody String requestBody) {
        return historicoEscolarWriteUseCase.criar(authorization, context, requestBody);
    }

    @PutMapping("/historicos-escolares/{id}")
    public ResponseEntity<String> atualizarHistoricoEscolar(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id,
            @RequestBody String requestBody) {
        return historicoEscolarWriteUseCase.atualizar(authorization, context, id, requestBody);
    }

    @GetMapping("/historicos-escolares/novo")
    public HistoricoEscolarTelaResponse carregarHistoricoNovo(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam UUID idAluno,
            @RequestParam UUID idMatricula,
            @RequestParam(defaultValue = "CADASTRO") String modo) {
        return historicoEscolarReadUseCase.carregarNovo(authorization, context, idAluno, idMatricula, modo);
    }

    @GetMapping("/historicos-escolares/{id}/carregamento")
    public HistoricoEscolarTelaResponse carregarHistoricoParaEdicao(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return historicoEscolarReadUseCase.carregarParaEdicao(authorization, context, id);
    }
}
