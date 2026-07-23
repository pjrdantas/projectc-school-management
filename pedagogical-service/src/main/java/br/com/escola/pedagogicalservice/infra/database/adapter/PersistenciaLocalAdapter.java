package br.com.escola.pedagogicalservice.infra.database.adapter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import br.com.escola.pedagogicalservice.application.context.InternalRequestContext;
import br.com.escola.pedagogicalservice.application.dto.AulaResponse;
import br.com.escola.pedagogicalservice.application.dto.AvaliacaoResponse;
import br.com.escola.pedagogicalservice.application.dto.BoletimIndicadoresResponse;
import br.com.escola.pedagogicalservice.application.dto.BoletimItemResponse;
import br.com.escola.pedagogicalservice.application.dto.BoletimResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaAlunoResponse;
import br.com.escola.pedagogicalservice.application.dto.FrequenciaDocenteResponse;
import br.com.escola.pedagogicalservice.application.dto.HistoricoEscolarTelaResponse;
import br.com.escola.pedagogicalservice.application.dto.NotaAlunoResponse;
import br.com.escola.pedagogicalservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.pedagogicalservice.application.exception.ConflitoNegocioException;
import br.com.escola.pedagogicalservice.infra.database.entity.AulaJpaEntity;
import br.com.escola.pedagogicalservice.infra.database.entity.AvaliacaoJpaEntity;
import br.com.escola.pedagogicalservice.infra.database.entity.DiarioClasseJpaEntity;
import br.com.escola.pedagogicalservice.infra.database.entity.FrequenciaAlunoJpaEntity;
import br.com.escola.pedagogicalservice.infra.database.entity.FrequenciaDocenteJpaEntity;
import br.com.escola.pedagogicalservice.infra.database.entity.HistoricoEscolarJpaEntity;
import br.com.escola.pedagogicalservice.infra.database.entity.NotaAlunoJpaEntity;
import br.com.escola.pedagogicalservice.infra.database.repository.AulaJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.AvaliacaoJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.BoletimJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.DiarioClasseJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.FrequenciaAlunoJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.FrequenciaDocenteJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.HistoricoEscolarJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.NotaAlunoJpaRepository;

@Component
@Transactional
public class PersistenciaLocalAdapter {

    private final ObjectMapper objectMapper;
    private final BoletimJpaRepository boletimRepository;
    private final AulaJpaRepository aulaRepository;
    private final FrequenciaDocenteJpaRepository frequenciaDocenteRepository;
    private final FrequenciaAlunoJpaRepository frequenciaAlunoRepository;
    private final AvaliacaoJpaRepository avaliacaoRepository;
    private final NotaAlunoJpaRepository notaAlunoRepository;
    private final DiarioClasseJpaRepository diarioClasseRepository;
    private final HistoricoEscolarJpaRepository historicoEscolarRepository;

    public PersistenciaLocalAdapter(
            ObjectMapper objectMapper,
            BoletimJpaRepository boletimRepository,
            AulaJpaRepository aulaRepository,
            FrequenciaDocenteJpaRepository frequenciaDocenteRepository,
            FrequenciaAlunoJpaRepository frequenciaAlunoRepository,
            AvaliacaoJpaRepository avaliacaoRepository,
            NotaAlunoJpaRepository notaAlunoRepository,
            DiarioClasseJpaRepository diarioClasseRepository,
            HistoricoEscolarJpaRepository historicoEscolarRepository) {
        this.objectMapper = objectMapper;
        this.boletimRepository = boletimRepository;
        this.aulaRepository = aulaRepository;
        this.frequenciaDocenteRepository = frequenciaDocenteRepository;
        this.frequenciaAlunoRepository = frequenciaAlunoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.notaAlunoRepository = notaAlunoRepository;
        this.diarioClasseRepository = diarioClasseRepository;
        this.historicoEscolarRepository = historicoEscolarRepository;
    }

    @Transactional(readOnly = true)
    public BoletimResponse consultarBoletimPorMatricula(String authorization, InternalRequestContext context, UUID matriculaId) {
        return boletimRepository.findFirstBySchoolIdAndMatriculaIdAndFechamentoFalseOrderByUpdatedAtDesc(context.escolaId(), matriculaId)
                .map(entity -> fromJson(entity.getPayloadJson(), BoletimResponse.class))
                .orElseGet(() -> gerarBoletimAtual(context, matriculaId));
    }

    @Transactional(readOnly = true)
    public List<BoletimResponse> listarFechamentosPorMatricula(String authorization, InternalRequestContext context, UUID matriculaId) {
        return boletimRepository.findAllBySchoolIdAndMatriculaIdAndFechamentoTrueOrderByUpdatedAtDescIdAsc(context.escolaId(), matriculaId)
                .stream()
                .map(entity -> fromJson(entity.getPayloadJson(), BoletimResponse.class))
                .toList();
    }

    public AulaResponse criarAula(String authorization, InternalRequestContext context, String requestBody) {
        JsonNode root = readTree(requestBody);
        var now = LocalDateTime.now();
        AulaResponse response = new AulaResponse(
                UUID.randomUUID(),
                uuid(root, "professorTurmaDisciplinaId"),
                uuid(root, "professorId"),
                text(root, "professorNome"),
                uuid(root, "turmaId"),
                text(root, "turmaNome"),
                context.escolaId(),
                text(root, "escolaNome"),
                uuid(root, "disciplinaId"),
                text(root, "disciplinaNome"),
                localDate(root, "dataAula"),
                localTime(root, "horarioInicio"),
                localTime(root, "horarioFim"),
                text(root, "conteudoMinistrado"),
                text(root, "observacao"),
                bool(root, "realizada"),
                now);
        aulaRepository.save(new AulaJpaEntity(
                response.id(),
                context.escolaId(),
                response.professorTurmaDisciplinaId(),
                response.turmaId(),
                toJson(response),
                now));
        return response;
    }

    @Transactional(readOnly = true)
    public List<AulaResponse> listarAulas(String authorization, InternalRequestContext context, UUID professorTurmaDisciplinaId, UUID turmaId) {
        return aulaRepository.findAllBySchoolIdOrderByCreatedAtDescIdAsc(context.escolaId()).stream()
                .map(entity -> fromJson(entity.getPayloadJson(), AulaResponse.class))
                .filter(response -> professorTurmaDisciplinaId == null || professorTurmaDisciplinaId.equals(response.professorTurmaDisciplinaId()))
                .filter(response -> turmaId == null || turmaId.equals(response.turmaId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AulaResponse buscarAulaPorId(String authorization, InternalRequestContext context, UUID aulaId) {
        return aulaRepository.findByIdAndSchoolId(aulaId, context.escolaId())
                .map(entity -> fromJson(entity.getPayloadJson(), AulaResponse.class))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula nao encontrada"));
    }

    public FrequenciaDocenteResponse registrarFrequenciaProfessor(
            String authorization,
            InternalRequestContext context,
            UUID aulaId,
            String requestBody) {
        garantirAula(context, aulaId);
        JsonNode root = readTree(requestBody);
        var now = LocalDateTime.now();
        FrequenciaDocenteResponse response = new FrequenciaDocenteResponse(
                UUID.randomUUID(),
                aulaId,
                uuid(root, "professorId"),
                text(root, "professorNome"),
                context.escolaId(),
                text(root, "escolaNome"),
                bool(root, "presente"),
                text(root, "justificativa"),
                now);
        frequenciaDocenteRepository.save(new FrequenciaDocenteJpaEntity(
                response.id(),
                context.escolaId(),
                aulaId,
                toJson(response),
                now));
        return response;
    }

    @Transactional(readOnly = true)
    public List<FrequenciaDocenteResponse> listarFrequenciaProfessor(String authorization, InternalRequestContext context, UUID aulaId) {
        garantirAula(context, aulaId);
        return frequenciaDocenteRepository.findAllBySchoolIdAndAulaIdOrderByCreatedAtAscIdAsc(context.escolaId(), aulaId).stream()
                .map(entity -> fromJson(entity.getPayloadJson(), FrequenciaDocenteResponse.class))
                .toList();
    }

    public FrequenciaAlunoResponse registrarFrequenciaAluno(
            String authorization,
            InternalRequestContext context,
            UUID aulaId,
            String requestBody) {
        garantirAula(context, aulaId);
        JsonNode root = readTree(requestBody);
        var now = LocalDateTime.now();
        FrequenciaAlunoResponse response = new FrequenciaAlunoResponse(
                UUID.randomUUID(),
                aulaId,
                uuid(root, "matriculaId"),
                uuid(root, "alunoId"),
                text(root, "alunoNome"),
                context.escolaId(),
                text(root, "escolaNome"),
                text(root, "situacao"),
                text(root, "justificativa"),
                now);
        frequenciaAlunoRepository.save(new FrequenciaAlunoJpaEntity(
                response.id(),
                context.escolaId(),
                aulaId,
                response.matriculaId(),
                toJson(response),
                now));
        return response;
    }

    @Transactional(readOnly = true)
    public List<FrequenciaAlunoResponse> listarFrequenciasAlunos(String authorization, InternalRequestContext context, UUID aulaId) {
        garantirAula(context, aulaId);
        return frequenciaAlunoRepository.findAllBySchoolIdAndAulaIdOrderByCreatedAtAscIdAsc(context.escolaId(), aulaId).stream()
                .map(entity -> fromJson(entity.getPayloadJson(), FrequenciaAlunoResponse.class))
                .toList();
    }

    public AvaliacaoResponse criarAvaliacao(String authorization, InternalRequestContext context, String requestBody) {
        JsonNode root = readTree(requestBody);
        var now = LocalDateTime.now();
        AvaliacaoResponse response = new AvaliacaoResponse(
                UUID.randomUUID(),
                uuid(root, "professorTurmaDisciplinaId"),
                uuid(root, "professorId"),
                text(root, "professorNome"),
                uuid(root, "turmaId"),
                text(root, "turmaNome"),
                context.escolaId(),
                text(root, "escolaNome"),
                uuid(root, "disciplinaId"),
                text(root, "disciplinaNome"),
                text(root, "titulo"),
                text(root, "descricao"),
                localDate(root, "dataAplicacao"),
                decimal(root, "valorMaximo"),
                decimal(root, "peso"),
                text(root, "tipoAvaliacao"),
                now);
        avaliacaoRepository.save(new AvaliacaoJpaEntity(
                response.id(),
                context.escolaId(),
                response.professorTurmaDisciplinaId(),
                response.turmaId(),
                response.titulo(),
                toJson(response),
                now));
        return response;
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoResponse> listarAvaliacoes(String authorization, InternalRequestContext context, UUID professorTurmaDisciplinaId, UUID turmaId) {
        return avaliacaoRepository.findAllBySchoolIdOrderByCreatedAtDescIdAsc(context.escolaId()).stream()
                .map(entity -> fromJson(entity.getPayloadJson(), AvaliacaoResponse.class))
                .filter(response -> professorTurmaDisciplinaId == null || professorTurmaDisciplinaId.equals(response.professorTurmaDisciplinaId()))
                .filter(response -> turmaId == null || turmaId.equals(response.turmaId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponse buscarAvaliacaoPorId(String authorization, InternalRequestContext context, UUID avaliacaoId) {
        return buscarAvaliacaoEntity(context, avaliacaoId)
                .map(entity -> fromJson(entity.getPayloadJson(), AvaliacaoResponse.class))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliacao nao encontrada"));
    }

    public NotaAlunoResponse lancarNota(String authorization, InternalRequestContext context, UUID avaliacaoId, String requestBody) {
        AvaliacaoResponse avaliacao = buscarAvaliacaoPorId(authorization, context, avaliacaoId);
        JsonNode root = readTree(requestBody);
        var now = LocalDateTime.now();
        NotaAlunoResponse response = new NotaAlunoResponse(
                UUID.randomUUID(),
                avaliacaoId,
                avaliacao.titulo(),
                uuid(root, "matriculaId"),
                uuid(root, "alunoId"),
                text(root, "alunoNome"),
                context.escolaId(),
                text(root, "escolaNome"),
                decimal(root, "nota"),
                text(root, "observacao"),
                now,
                now);
        notaAlunoRepository.save(new NotaAlunoJpaEntity(
                response.id(),
                context.escolaId(),
                avaliacaoId,
                response.matriculaId(),
                toJson(response),
                now));
        return response;
    }

    @Transactional(readOnly = true)
    public List<NotaAlunoResponse> listarNotasPorAvaliacao(String authorization, InternalRequestContext context, UUID avaliacaoId) {
        buscarAvaliacaoPorId(authorization, context, avaliacaoId);
        return notaAlunoRepository.findAllBySchoolIdAndAvaliacaoIdOrderByCreatedAtAscIdAsc(context.escolaId(), avaliacaoId).stream()
                .map(entity -> fromJson(entity.getPayloadJson(), NotaAlunoResponse.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotaAlunoResponse> listarNotasPorMatricula(String authorization, InternalRequestContext context, UUID matriculaId) {
        return notaAlunoRepository.findAllBySchoolIdAndMatriculaIdOrderByCreatedAtAscIdAsc(context.escolaId(), matriculaId).stream()
                .map(entity -> fromJson(entity.getPayloadJson(), NotaAlunoResponse.class))
                .toList();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<String> carregarDiarioClasse(
            String authorization,
            InternalRequestContext context,
            UUID professorId,
            UUID turmaId,
            UUID disciplinaId,
            Integer anoLetivo,
            Integer mes,
            LocalDate dataReferencia) {
        DiarioClasseJpaEntity entity = diarioClasseRepository
                .findFirstBySchoolIdAndProfessorIdAndTurmaIdAndDisciplinaIdAndAnoLetivoAndMesAndDataReferenciaOrderByUpdatedAtDesc(
                        context.escolaId(),
                        professorId,
                        turmaId,
                        disciplinaId,
                        anoLetivo,
                        mes,
                        dataReferencia)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Diario de classe nao encontrado"));
        return ResponseEntity.ok(entity.getPayloadLeitura());
    }

    public ResponseEntity<String> salvarDiarioClasse(String authorization, InternalRequestContext context, String idDiarioClasse, String requestBody) {
        var now = LocalDateTime.now();
        DiarioClasseJpaEntity atual = diarioClasseRepository.findByIdAndSchoolId(idDiarioClasse, context.escolaId()).orElse(null);
        String responseBody = buildDiaryWriteResponse(idDiarioClasse);
        DiarioClasseJpaEntity entity = new DiarioClasseJpaEntity(
                idDiarioClasse,
                context.escolaId(),
                atual == null ? null : atual.getProfessorId(),
                atual == null ? null : atual.getTurmaId(),
                atual == null ? null : atual.getDisciplinaId(),
                atual == null ? null : atual.getAnoLetivo(),
                atual == null ? null : atual.getMes(),
                atual == null ? null : atual.getDataReferencia(),
                atual == null ? null : atual.getPayloadLeitura(),
                responseBody,
                now);
        diarioClasseRepository.save(entity);
        return ResponseEntity.ok(responseBody);
    }

    public ResponseEntity<String> criarHistoricoEscolar(String authorization, InternalRequestContext context, String requestBody) {
        JsonNode root = readTree(requestBody);
        validarItensETransferencia(root);
        UUID id = UUID.randomUUID();
        UUID alunoId = uuid(root, "alunoId");
        UUID matriculaId = uuid(root, "matriculaId");
        String responseBody = buildHistoricoWriteResponse(id, root);
        HistoricoEscolarTelaResponse tela = buildHistoricoTela(id, alunoId, matriculaId, "CADASTRO", root);
        historicoEscolarRepository.save(new HistoricoEscolarJpaEntity(
                id,
                context.escolaId(),
                alunoId,
                matriculaId,
                "CADASTRO",
                toJson(tela),
                responseBody,
                LocalDateTime.now()));
        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    public ResponseEntity<String> atualizarHistoricoEscolar(String authorization, InternalRequestContext context, UUID historicoEscolarId, String requestBody) {
        JsonNode root = readTree(requestBody);
        validarItensETransferencia(root);
        HistoricoEscolarJpaEntity atual = historicoEscolarRepository.findByIdAndSchoolId(historicoEscolarId, context.escolaId())
                .orElse(null);
        if (atual == null) {
            throw new RecursoNaoEncontradoException("Historico escolar nao encontrado");
        }
        validarTransicaoHistorico(atual, root);
        UUID alunoId = uuid(root, "alunoId");
        UUID matriculaId = atual != null ? atual.getMatriculaId() : uuid(root, "matriculaId");
        String modo = atual != null ? atual.getModo() : "EDICAO";
        HistoricoEscolarTelaResponse tela = buildHistoricoTela(historicoEscolarId, alunoId, matriculaId, modo, root);
        String responseBody = buildHistoricoWriteResponse(historicoEscolarId, root);
        historicoEscolarRepository.save(new HistoricoEscolarJpaEntity(
                historicoEscolarId,
                context.escolaId(),
                alunoId,
                matriculaId,
                modo,
                toJson(tela),
                responseBody,
                LocalDateTime.now()));
        return ResponseEntity.ok(responseBody);
    }

    @Transactional(readOnly = true)
    public HistoricoEscolarTelaResponse carregarNovo(
            String authorization,
            InternalRequestContext context,
            UUID alunoId,
            UUID matriculaId,
            String modo) {
        return historicoEscolarRepository.findFirstBySchoolIdAndAlunoIdAndMatriculaIdAndModoOrderByUpdatedAtDesc(
                        context.escolaId(),
                        alunoId,
                        matriculaId,
                        modo)
                .map(entity -> fromJson(entity.getPayloadTela(), HistoricoEscolarTelaResponse.class))
                .orElseGet(() -> buildHistoricoTela(null, alunoId, matriculaId, modo, null));
    }

    @Transactional(readOnly = true)
    public HistoricoEscolarTelaResponse carregarHistoricoParaEdicao(String authorization, InternalRequestContext context, UUID historicoEscolarId) {
        return historicoEscolarRepository.findByIdAndSchoolId(historicoEscolarId, context.escolaId())
                .map(entity -> fromJson(entity.getPayloadTela(), HistoricoEscolarTelaResponse.class))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Historico escolar nao encontrado"));
    }

    @Transactional(readOnly = true)
    public String listarHistoricosEscolares(InternalRequestContext context, int page, int size) {
        List<HistoricoEscolarJpaEntity> itens = historicoEscolarRepository.findBySchoolIdOrderByUpdatedAtDesc(context.escolaId());
        int pagina = Math.max(page, 0);
        int tamanho = Math.max(size, 1);
        int inicio = Math.min(pagina * tamanho, itens.size());
        int fim = Math.min(inicio + tamanho, itens.size());
        ObjectNode resposta = objectMapper.createObjectNode();
        var content = resposta.putArray("content");
        itens.subList(inicio, fim).forEach(item -> content.add(readTree(item.getPayloadEscrita())));
        resposta.put("totalElements", itens.size());
        resposta.put("totalPages", (int) Math.ceil((double) itens.size() / tamanho));
        resposta.put("size", tamanho);
        resposta.put("number", pagina);
        resposta.put("first", pagina == 0);
        resposta.put("last", fim == itens.size());
        resposta.put("empty", itens.isEmpty());
        return resposta.toString();
    }

    @Transactional(readOnly = true)
    public String listarHistoricosEscolaresPorAluno(InternalRequestContext context, UUID alunoId) {
        var resposta = objectMapper.createArrayNode();
        historicoEscolarRepository.findBySchoolIdAndAlunoIdOrderByUpdatedAtDesc(context.escolaId(), alunoId)
                .forEach(item -> resposta.add(readTree(item.getPayloadEscrita())));
        return resposta.toString();
    }

    @Transactional
    public void excluirHistoricoEscolar(InternalRequestContext context, UUID historicoEscolarId) {
        HistoricoEscolarJpaEntity item = historicoEscolarRepository.findByIdAndSchoolId(historicoEscolarId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Historico escolar nao encontrado"));
        historicoEscolarRepository.delete(item);
    }

    private void garantirAula(InternalRequestContext context, UUID aulaId) {
        aulaRepository.findByIdAndSchoolId(aulaId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aula nao encontrada"));
    }

    private java.util.Optional<AvaliacaoJpaEntity> buscarAvaliacaoEntity(InternalRequestContext context, UUID avaliacaoId) {
        return avaliacaoRepository.findByIdAndSchoolId(avaliacaoId, context.escolaId());
    }

    private BoletimResponse gerarBoletimAtual(InternalRequestContext context, UUID matriculaId) {
        List<NotaAlunoResponse> notas = listarNotasPorMatricula("", context, matriculaId);
        if (notas.isEmpty()) {
            throw new RecursoNaoEncontradoException("Boletim nao encontrado");
        }
        List<BoletimItemResponse> itens = notas.stream()
                .map(nota -> new BoletimItemResponse(
                        null,
                        nota.avaliacaoTitulo(),
                        nota.nota(),
                        BigDecimal.ZERO,
                        1,
                        0,
                        nota.nota() != null && nota.nota().compareTo(new BigDecimal("6.0")) >= 0 ? "APROVADO" : "EM_ANALISE"))
                .toList();
        BigDecimal media = notas.stream()
                .map(NotaAlunoResponse::nota)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(itens.size()), 2, java.math.RoundingMode.HALF_UP);
        NotaAlunoResponse primeira = notas.getFirst();
        return new BoletimResponse(
                UUID.randomUUID(),
                matriculaId,
                primeira.alunoId(),
                primeira.alunoNome(),
                null,
                null,
                null,
                null,
                context.escolaId(),
                primeira.escolaNome(),
                LocalDate.now(),
                "ATUAL",
                null,
                null,
                true,
                new BoletimIndicadoresResponse(itens.size(), media, BigDecimal.ZERO, "EM_ANALISE"),
                itens);
    }

    private HistoricoEscolarTelaResponse buildHistoricoTela(UUID id, UUID alunoId, UUID matriculaId, String modo, JsonNode root) {
        if (root != null && root.has("cabecalho") && root.has("aluno")) {
            try {
                ObjectNode tela = root.deepCopy();
                ObjectNode contexto = tela.withObject("contexto");
                contexto.put("idHistoricoEscolar", id == null ? null : id.toString());
                contexto.put("idAluno", alunoId == null ? null : alunoId.toString());
                contexto.put("idMatricula", matriculaId == null ? null : matriculaId.toString());
                contexto.put("modo", modo);
                contexto.put("status", root.path("statusPretendido").asText("RASCUNHO"));
                contexto.put("bloqueado", "COMPLETO".equals(contexto.path("status").asText()));
                if (!tela.has("pendencias")) {
                    tela.set("pendencias", objectMapper.createArrayNode());
                }
                return objectMapper.treeToValue(tela, HistoricoEscolarTelaResponse.class);
            } catch (Exception exception) {
                throw new IllegalArgumentException("Payload de historico escolar invalido", exception);
            }
        }
        String observacoes = root != null ? text(root, "observacoes") : null;
        return new HistoricoEscolarTelaResponse(
                new HistoricoEscolarTelaResponse.Contexto(
                        id,
                        alunoId,
                        matriculaId,
                        modo,
                        "RASCUNHO",
                        null,
                        null,
                        null,
                        null,
                        false),
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of(),
                observacoes,
                null,
                List.of());
    }

    private String buildHistoricoWriteResponse(UUID id, JsonNode root) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("id", id.toString());
        response.put("status", root.path("statusPretendido").asText("RASCUNHO"));
        response.put("bloqueado", "COMPLETO".equals(root.path("statusPretendido").asText()));
        copyIfPresent(root, response, "alunoId");
        copyIfPresent(root, response, "nomeAluno");
        if (root != null && root.has("componentesCurriculares")) {
            response.set("componentesCurriculares", root.get("componentesCurriculares"));
        }
        return writeString(response);
    }

    private void validarTransicaoHistorico(HistoricoEscolarJpaEntity atual, JsonNode root) {
        HistoricoEscolarTelaResponse telaAtual = fromJson(atual.getPayloadTela(), HistoricoEscolarTelaResponse.class);
        String atualStatus = telaAtual.contexto().status();
        String pretendido = root.path("statusPretendido").asText("RASCUNHO");
        boolean reabrir = root.path("reabrir").asBoolean(false);
        if ("COMPLETO".equals(atualStatus)) {
            if (!reabrir || root.path("justificativaReabertura").asText().isBlank() || !"RASCUNHO".equals(pretendido)) {
                throw new ConflitoNegocioException("Historico completo esta bloqueado; reabertura exige justificativa e retorno para RASCUNHO");
            }
            return;
        }
        if ("PENDENTE".equals(atualStatus) && "COMPLETO".equals(pretendido)) {
            throw new ConflitoNegocioException("Historico pendente nao pode ser concluido sem saneamento");
        }
        if (!java.util.Set.of("RASCUNHO", "PENDENTE", "COMPLETO").contains(pretendido)) {
            throw new ConflitoNegocioException("Status de historico escolar invalido");
        }
    }

    private void validarItensETransferencia(JsonNode root) {
        JsonNode periodos = root.path("periodos");
        if (periodos.isArray()) {
            int ordemEsperada = 1;
            for (JsonNode periodo : periodos) {
                if (periodo.path("ordem").asInt() != ordemEsperada++
                        || periodo.path("anoLetivo").asText().isBlank()
                        || periodo.path("serie").asText().isBlank()) {
                    throw new ConflitoNegocioException("Periodos do historico devem ter ordem sequencial, ano e serie");
                }
            }
            validarComponentes(root.path("baseComum"), periodos.size());
            validarComponentes(root.path("parteDiversificada"), periodos.size());
        }
        JsonNode contexto = root.path("contexto");
        boolean temOrigem = !contexto.path("escolaOrigem").asText().isBlank();
        boolean temTransferencia = !contexto.path("dataTransferencia").asText().isBlank();
        if (temOrigem != temTransferencia) {
            throw new ConflitoNegocioException("Transferencia exige escola de origem e data de transferencia");
        }
        if (temOrigem && contexto.path("serieConcluidaOrigem").asInt(0) <= 0) {
            throw new ConflitoNegocioException("Transferencia exige serie concluida na origem");
        }
    }

    private void validarComponentes(JsonNode componentes, int quantidadePeriodos) {
        if (!componentes.isArray()) return;
        for (JsonNode componente : componentes) {
            if (componente.path("nome").asText().isBlank()
                    || !componente.path("valores").isArray()
                    || componente.path("valores").size() != quantidadePeriodos) {
                throw new ConflitoNegocioException("Componentes curriculares devem possuir nome e um valor por periodo");
            }
        }
    }

    private String buildDiaryWriteResponse(String idDiarioClasse) {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("idDiarioClasse", idDiarioClasse);
        response.put("status", "SALVO");
        response.put("mensagem", "Lancamento salvo com sucesso.");
        response.put("salvoEm", LocalDateTime.now().toString());
        response.put("bloqueado", true);
        return writeString(response);
    }

    private void copyIfPresent(JsonNode source, ObjectNode target, String field) {
        if (source != null && source.has(field)) {
            target.set(field, source.get(field));
        }
    }

    private JsonNode readTree(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Payload JSON invalido", exception);
        }
    }

    private <T> T fromJson(String payload, Class<T> type) {
        try {
            return objectMapper.readValue(payload, type);
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao converter payload persistido", exception);
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao serializar payload", exception);
        }
    }

    private String writeString(ObjectNode payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao montar resposta JSON", exception);
        }
    }

    private UUID uuid(JsonNode root, String field) {
        String value = text(root, field);
        return value == null || value.isBlank() ? null : UUID.fromString(value);
    }

    private String text(JsonNode root, String field) {
        return root != null && root.hasNonNull(field) ? root.get(field).asText() : null;
    }

    private Boolean bool(JsonNode root, String field) {
        return root != null && root.has(field) && !root.get(field).isNull() ? root.get(field).asBoolean() : null;
    }

    private BigDecimal decimal(JsonNode root, String field) {
        return root != null && root.has(field) && !root.get(field).isNull() ? root.get(field).decimalValue() : null;
    }

    private LocalDate localDate(JsonNode root, String field) {
        String value = text(root, field);
        return value == null || value.isBlank() ? null : LocalDate.parse(value);
    }

    private LocalTime localTime(JsonNode root, String field) {
        String value = text(root, field);
        return value == null || value.isBlank() ? null : LocalTime.parse(value);
    }
}
