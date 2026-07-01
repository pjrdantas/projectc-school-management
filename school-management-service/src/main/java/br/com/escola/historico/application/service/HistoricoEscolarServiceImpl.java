package br.com.escola.historico.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;
import br.com.escola.aluno.application.port.internal.AlunoMatriculaPort;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarGeracaoRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarItemRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarResponse;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarTelaResponse;
import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolar;
import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolarItem;
import br.com.escola.historico.adapter.out.persistence.repository.HistoricoEscolarJpaRepository;
import br.com.escola.historico.application.port.internal.BoletimHistoricoPort;
import br.com.escola.historico.application.mapper.HistoricoEscolarMapper;
import br.com.escola.historico.domain.exception.BoletimFechadoNaoEncontradoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarDuplicadoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarInvalidoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarNaoEncontradoException;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.matricula.adapter.out.persistence.entity.MatriculaEntity;
import br.com.escola.matricula.adapter.out.persistence.repository.MatriculaJpaRepository;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;
import br.com.escola.transferencia.adapter.out.persistence.entity.TransferenciaAlunoEntity;
import br.com.escola.transferencia.adapter.out.persistence.repository.TransferenciaAlunoJpaRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoricoEscolarServiceImpl implements HistoricoEscolarService {

    private static final String STATUS_RASCUNHO = "RASCUNHO";
    private static final String STATUS_PENDENTE = "PENDENTE";
    private static final String GOVERNO_PADRAO = "GOVERNO DO ESTADO DE SÃO PAULO";
    private static final String SECRETARIA_PADRAO = "SECRETARIA DE ESTADO DA EDUCAÇÃO";
    private static final String ATO_LEGAL_LABEL = "Ato Legal de criação:";
    private static final int TOTAL_PERIODOS_PADRAO = 9;
    private static final Pattern PRIMEIRO_NUMERO = Pattern.compile("(\\d+)");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final HistoricoEscolarJpaRepository historicoEscolarJpaRepository;
    private final BoletimHistoricoPort boletimHistoricoPort;
    private final HistoricoEscolarGeracaoFactory historicoEscolarGeracaoFactory;
    private final HistoricoEscolarMapper historicoEscolarMapper;
    private final AlunoMatriculaPort alunoMatriculaPort;
    private final MatriculaJpaRepository matriculaJpaRepository;
    private final TransferenciaAlunoJpaRepository transferenciaAlunoJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    @Override
    @Transactional
    public HistoricoEscolarResponse criar(HistoricoEscolarRequest request) {
        validarRequest(request);
        var historico = historicoEscolarMapper.toEntity(request);
        historico.setAluno(alunoEscopado(request.alunoId()));
        preencherContextoPersistido(historico);
        var salvo = historicoEscolarJpaRepository.save(historico);
        return historicoEscolarMapper.toResponse(buscarHistoricoEscopado(salvo.getId()));
    }

    @Override
    @Transactional
    public HistoricoEscolarResponse atualizar(UUID id, HistoricoEscolarRequest request) {
        validarRequest(request);
        var historico = historicoEscolarJpaRepository.findWithComponentesCurricularesByIdAndEscolaId(id, escolaId())
                .orElseThrow(() -> new HistoricoEscolarNaoEncontradoException(id));
        historicoEscolarMapper.copyToEntity(request, historico);
        historico.setAluno(alunoEscopado(request.alunoId()));
        preencherContextoPersistido(historico);
        var salvo = historicoEscolarJpaRepository.save(historico);
        return historicoEscolarMapper.toResponse(buscarHistoricoEscopado(salvo.getId()));
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        var historico = historicoEscolarJpaRepository.findWithComponentesCurricularesByIdAndEscolaId(id, escolaId())
                .orElseThrow(() -> new HistoricoEscolarNaoEncontradoException(id));
        historicoEscolarJpaRepository.delete(historico);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoricoEscolarResponse buscarPorId(UUID id) {
        return historicoEscolarJpaRepository.findWithComponentesCurricularesByIdAndEscolaId(id, escolaId())
                .map(historicoEscolarMapper::toResponse)
                .orElseThrow(() -> new HistoricoEscolarNaoEncontradoException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoricoEscolarResponse> listarPorAluno(UUID alunoId) {
        UUID escolaId = escolaId();
        if (!alunoMatriculaPort.existeAlunoPorIdEEscola(alunoId, escolaId)) {
            throw new HistoricoEscolarInvalidoException("Aluno do histórico escolar não encontrado");
        }
        return historicoEscolarJpaRepository.findByAlunoIdAndAluno_Pessoa_Escola_Id(alunoId, escolaId).stream()
                .map(historicoEscolarMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoricoEscolarResponse> listar(Pageable pageable) {
        return historicoEscolarJpaRepository.findByAluno_Pessoa_Escola_Id(escolaId(), pageable)
                .map(historicoEscolarMapper::toResponse);
    }

    @Override
    @Transactional
    public HistoricoEscolarResponse gerarPorBoletim(UUID matriculaId, HistoricoEscolarGeracaoRequest request) {
        UUID escolaId = escolaId();
        var boletim = boletimHistoricoPort.buscarParaGeracao(request.boletimId(), escolaId)
                .orElseThrow(() -> new BoletimFechadoNaoEncontradoException(request.boletimId()));
        if (!boletim.matriculaId().equals(matriculaId)) {
            throw new MatriculaNaoEncontradaException(matriculaId);
        }

        UUID alunoId = boletim.alunoId();
        List<HistoricoEscolar> duplicados = historicoEscolarJpaRepository
                .findByAlunoIdAndEscolaIdAndPeriodoLetivoId(alunoId, escolaId, boletim.periodoLetivoId());
        if (!duplicados.isEmpty() && !Boolean.TRUE.equals(request.sobrescrever())) {
            throw new HistoricoEscolarDuplicadoException(alunoId, boletim.periodoLetivoId());
        }
        duplicados.forEach(historicoEscolarJpaRepository::delete);

        if (boletim.itens().isEmpty()) {
            throw new HistoricoEscolarInvalidoException("Boletim fechado não possui itens para geração do histórico escolar");
        }

        HistoricoEscolar historico = historicoEscolarGeracaoFactory.criar(request, boletim, alunoEscopado(alunoId));
        preencherContextoPersistido(historico, matriculaId, null);

        var salvo = historicoEscolarJpaRepository.save(historico);
        return historicoEscolarMapper.toResponse(buscarHistoricoEscopado(salvo.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public HistoricoEscolarTelaResponse carregarNovo(UUID alunoId, UUID matriculaId, String modo) {
        AlunoEntity aluno = alunoEscopado(alunoId);
        MatriculaEntity matricula = matriculaEscopada(matriculaId);
        if (!matricula.getAluno().getId().equals(alunoId)) {
            throw new MatriculaNaoEncontradaException(matriculaId);
        }

        TransferenciaAlunoEntity transferencia = transferenciaAlunoJpaRepository.findByAluno_IdOrderByCreatedAtDesc(alunoId).stream()
                .findFirst()
                .orElse(null);

        Integer serieAtual = serieAtual(matricula);
        Integer serieConcluidaOrigem = serieConcluidaOrigem(transferencia);

        return new HistoricoEscolarTelaResponse(
                new HistoricoEscolarTelaResponse.Contexto(
                        null,
                        alunoId,
                        matriculaId,
                        blankToDefault(modo, "CADASTRO"),
                        "RASCUNHO",
                        serieAtual,
                        serieConcluidaOrigem,
                        transferencia == null || transferencia.getEscolaOrigem() == null ? "" : nullToEmpty(transferencia.getEscolaOrigem().getNome()),
                        formatDate(transferencia == null ? null : transferencia.getDataTransferencia()),
                        false),
                cabecalhoNovo(matricula),
                alunoNovo(aluno),
                periodosPadrao(),
                List.of(),
                List.of(),
                totaisVazios(TOTAL_PERIODOS_PADRAO),
                List.of(estudoRealizadoVazio(1)),
                "",
                certificadoNovo(aluno, transferencia),
                pendenciasNovo(serieAtual, serieConcluidaOrigem));
    }

    @Override
    @Transactional(readOnly = true)
    public HistoricoEscolarTelaResponse carregarParaEdicao(UUID id) {
        HistoricoEscolar historico = buscarHistoricoEscopado(id);
        MatriculaEntity matricula = matriculaAtual(historico.getAlunoId());
        TransferenciaAlunoEntity transferencia = transferenciaAlunoJpaRepository
                .findByAluno_IdOrderByCreatedAtDesc(historico.getAlunoId()).stream()
                .findFirst()
                .orElse(null);
        Integer serieAtual = serieAtual(matricula);
        Integer serieConcluidaOrigem = serieConcluidaOrigem(transferencia);
        List<HistoricoEscolarTelaResponse.Periodo> periodos = periodosDoHistorico(historico);

        return new HistoricoEscolarTelaResponse(
                new HistoricoEscolarTelaResponse.Contexto(
                        historico.getId(),
                        historico.getAlunoId(),
                        firstNonNull(historico.getMatriculaId(), matricula == null ? null : matricula.getId()),
                        "EDICAO",
                        blankToDefault(historico.getStatus(), STATUS_RASCUNHO),
                        firstNonNull(historico.getSerieMatriculaAtual(), serieAtual),
                        firstNonNull(historico.getSerieConcluidaOrigem(), serieConcluidaOrigem),
                        firstNonBlank(
                                historico.getEscolaOrigemNome(),
                                transferencia == null || transferencia.getEscolaOrigem() == null ? "" : transferencia.getEscolaOrigem().getNome()),
                        formatDate(firstNonNull(historico.getDataTransferencia(), transferencia == null ? null : transferencia.getDataTransferencia())),
                        Boolean.TRUE.equals(historico.getBloqueado())),
                cabecalhoHistorico(historico),
                alunoHistorico(historico),
                periodos,
                componentesDoHistorico(historico, periodos),
                List.of(),
                totaisVazios(periodos.size()),
                List.of(estudoRealizadoVazio(1)),
                nullToEmpty(historico.getObservacoes()),
                certificadoHistorico(historico),
                pendenciasHistorico(
                        historico,
                        firstNonNull(historico.getSerieMatriculaAtual(), serieAtual),
                        firstNonNull(historico.getSerieConcluidaOrigem(), serieConcluidaOrigem)));
    }

    private void validarRequest(HistoricoEscolarRequest request) {
        if (request.alunoId() != null
                && !alunoMatriculaPort.existeAlunoPorIdEEscola(request.alunoId(), escolaId())) {
            throw new HistoricoEscolarInvalidoException("Aluno do histórico escolar não encontrado");
        }
        if (request.componentesCurriculares() == null || request.componentesCurriculares().isEmpty()) {
            throw new HistoricoEscolarInvalidoException("Informe ao menos um componente curricular no histórico escolar");
        }
        validarComponentesDuplicados(request.componentesCurriculares());
    }

    private void validarComponentesDuplicados(List<HistoricoEscolarItemRequest> itens) {
        var chaves = new HashSet<String>();
        for (HistoricoEscolarItemRequest item : itens) {
            String chave = normalizar(item.componenteCurricular())
                    + "|" + (item.anoLetivo() == null ? "" : item.anoLetivo())
                    + "|" + normalizar(item.serie());
            if (!chaves.add(chave)) {
                throw new HistoricoEscolarInvalidoException(
                        "Componente curricular duplicado no histórico: %s, ano letivo %s, série %s"
                                .formatted(
                                        item.componenteCurricular(),
                                        item.anoLetivo() == null ? "não informado" : item.anoLetivo(),
                                        item.serie() == null || item.serie().isBlank() ? "não informada" : item.serie()));
            }
        }
    }

    private String normalizar(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private HistoricoEscolar buscarHistoricoEscopado(UUID id) {
        return historicoEscolarJpaRepository.findWithComponentesCurricularesByIdAndEscolaId(id, escolaId())
                .orElseThrow(() -> new HistoricoEscolarNaoEncontradoException(id));
    }

    private MatriculaEntity matriculaEscopada(UUID matriculaId) {
        return matriculaJpaRepository.findByIdAndTurma_Escola_Id(matriculaId, escolaId())
                .orElseThrow(() -> new MatriculaNaoEncontradaException(matriculaId));
    }

    private MatriculaEntity matriculaAtual(UUID alunoId) {
        return matriculaJpaRepository.findFirstByAluno_IdAndAluno_Pessoa_Escola_IdOrderByDataSolicitacaoDescCreatedAtDesc(alunoId, escolaId())
                .orElse(null);
    }

    private AlunoEntity alunoEscopado(UUID alunoId) {
        return alunoMatriculaPort.buscarAlunoPorIdEEscola(alunoId, escolaId())
                .orElseThrow(() -> new HistoricoEscolarInvalidoException("Aluno do histórico escolar não encontrado"));
    }

    private void preencherContextoPersistido(HistoricoEscolar historico) {
        preencherContextoPersistido(historico, null, null);
    }

    private void preencherContextoPersistido(
            HistoricoEscolar historico,
            UUID matriculaIdPreferencial,
            UUID transferenciaIdPreferencial) {
        MatriculaEntity matricula = matriculaIdPreferencial == null
                ? matriculaAtual(historico.getAlunoId())
                : matriculaJpaRepository.findByIdAndTurma_Escola_Id(matriculaIdPreferencial, escolaId()).orElse(null);
        TransferenciaAlunoEntity transferencia = transferenciaIdPreferencial == null
                ? transferenciaAlunoJpaRepository.findByAluno_IdOrderByCreatedAtDesc(historico.getAlunoId()).stream().findFirst().orElse(null)
                : transferenciaAlunoJpaRepository.findById(transferenciaIdPreferencial).orElse(null);

        historico.setMatriculaId(matricula == null ? null : matricula.getId());
        historico.setTransferenciaAlunoId(transferencia == null ? null : transferencia.getId());
        historico.setSerieMatriculaAtual(serieAtual(matricula));
        historico.setSerieConcluidaOrigem(serieConcluidaOrigem(transferencia));
        historico.setEscolaOrigemNome(
                transferencia == null || transferencia.getEscolaOrigem() == null
                        ? null
                        : trimToNull(transferencia.getEscolaOrigem().getNome()));
        historico.setDataTransferencia(transferencia == null ? null : transferencia.getDataTransferencia());
        historico.setBloqueado(false);
        historico.setStatus(STATUS_PENDENTE);
        historico.setAtualizadoEm(LocalDateTime.now());
    }

    private HistoricoEscolarTelaResponse.Cabecalho cabecalhoNovo(MatriculaEntity matricula) {
        var escola = matricula.getTurma().getEscola();
        return new HistoricoEscolarTelaResponse.Cabecalho(
                GOVERNO_PADRAO,
                SECRETARIA_PADRAO,
                "",
                escola == null ? "" : nullToEmpty(escola.getNome()),
                ATO_LEGAL_LABEL,
                "",
                "",
                "",
                "",
                "",
                "",
                escola == null ? "" : nullToEmpty(escola.getTelefone()),
                escola == null ? "" : nullToEmpty(escola.getEmail()));
    }

    private HistoricoEscolarTelaResponse.Aluno alunoNovo(AlunoEntity aluno) {
        return new HistoricoEscolarTelaResponse.Aluno(
                nullToEmpty(aluno.getNomeCompleto()),
                nullToEmpty(aluno.getRg()),
                nullToEmpty(aluno.getRa()),
                nullToEmpty(aluno.getNaturalidade()),
                "",
                nullToEmpty(aluno.getNacionalidade()),
                formatDate(aluno.getDataNascimento()));
    }

    private List<HistoricoEscolarTelaResponse.Periodo> periodosPadrao() {
        List<HistoricoEscolarTelaResponse.Periodo> periodos = new ArrayList<>();
        for (int ordem = 1; ordem <= TOTAL_PERIODOS_PADRAO; ordem++) {
            periodos.add(new HistoricoEscolarTelaResponse.Periodo(
                    ordem,
                    "",
                    ordem + "º Ano",
                    ordem + "ª série"));
        }
        return periodos;
    }

    private HistoricoEscolarTelaResponse.Totais totaisVazios(int totalPeriodos) {
        List<String> vazios = new ArrayList<>();
        for (int i = 0; i < totalPeriodos; i++) {
            vazios.add("");
        }
        return new HistoricoEscolarTelaResponse.Totais(vazios, vazios, vazios, vazios);
    }

    private HistoricoEscolarTelaResponse.EstudoRealizado estudoRealizadoVazio(int ordem) {
        return new HistoricoEscolarTelaResponse.EstudoRealizado(ordem, ordem + "º Ano", "", "", "", "");
    }

    private HistoricoEscolarTelaResponse.Certificado certificadoNovo(AlunoEntity aluno, TransferenciaAlunoEntity transferencia) {
        Integer serieConcluida = serieConcluidaOrigem(transferencia);
        String escolaOrigem = transferencia == null || transferencia.getEscolaOrigem() == null
                ? ""
                : nullToEmpty(transferencia.getEscolaOrigem().getNome());
        return new HistoricoEscolarTelaResponse.Certificado(
                serieConcluida,
                "",
                escolaOrigem,
                nullToEmpty(aluno.getRg()),
                "",
                "",
                "",
                "",
                "",
                "",
                "");
    }

    private List<HistoricoEscolarTelaResponse.Pendencia> pendenciasNovo(Integer serieAtual, Integer serieConcluidaOrigem) {
        List<HistoricoEscolarTelaResponse.Pendencia> pendencias = new ArrayList<>();
        if (serieAtual != null && serieAtual > 1) {
            pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                    "HISTORICO_SERIE_ANTERIOR_INCOMPLETO",
                    "AVISO",
                    "ANOS_COMPONENTES",
                    "Aluno matriculado em série posterior. O histórico precisa ser preenchido até a série anterior à matrícula."));
        }
        pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                "ANOS_COMPONENTES_PENDENTES",
                "AVISO",
                "ANOS_COMPONENTES",
                "Há anos letivos e componentes curriculares pendentes para preenchimento."));
        pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                "ESTUDOS_REALIZADOS_PENDENTE",
                "AVISO",
                "ESTUDOS_REALIZADOS",
                "Há estudos realizados pendentes para completar o histórico."));
        if (serieConcluidaOrigem == null) {
            pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                    "CERTIFICADO_PENDENTE",
                    "AVISO",
                    "OBSERVACOES_CERTIFICADO",
                    "A série concluída na escola de origem ainda precisa ser informada."));
        }
        return pendencias;
    }

    private HistoricoEscolarTelaResponse.Cabecalho cabecalhoHistorico(HistoricoEscolar historico) {
        String escolaAtual = historico.getAluno() == null || historico.getAluno().getPessoa() == null || historico.getAluno().getPessoa().getEscola() == null
                ? ""
                : nullToEmpty(historico.getAluno().getPessoa().getEscola().getNome());
        String telefoneAtual = historico.getAluno() == null || historico.getAluno().getPessoa() == null || historico.getAluno().getPessoa().getEscola() == null
                ? ""
                : nullToEmpty(historico.getAluno().getPessoa().getEscola().getTelefone());
        String emailAtual = historico.getAluno() == null || historico.getAluno().getPessoa() == null || historico.getAluno().getPessoa().getEscola() == null
                ? ""
                : nullToEmpty(historico.getAluno().getPessoa().getEscola().getEmail());

        return new HistoricoEscolarTelaResponse.Cabecalho(
                GOVERNO_PADRAO,
                SECRETARIA_PADRAO,
                "",
                firstNonBlank(historico.getNomeEscola(), escolaAtual),
                ATO_LEGAL_LABEL,
                "",
                nullToEmpty(historico.getEnderecoEscola()),
                "",
                "",
                nullToEmpty(historico.getMunicipioEscola()),
                nullToEmpty(historico.getCepEscola()),
                firstNonBlank(historico.getTelefoneEscola(), telefoneAtual),
                firstNonBlank(historico.getEmailEscola(), emailAtual));
    }

    private HistoricoEscolarTelaResponse.Aluno alunoHistorico(HistoricoEscolar historico) {
        return new HistoricoEscolarTelaResponse.Aluno(
                firstNonBlank(historico.getNomeAluno(), historico.getAluno() == null ? null : historico.getAluno().getNomeCompleto()),
                firstNonBlank(historico.getRgRen(), historico.getAluno() == null ? null : historico.getAluno().getRg()),
                firstNonBlank(historico.getRa(), historico.getAluno() == null ? null : historico.getAluno().getRa()),
                firstNonBlank(historico.getMunicipioNascimento(), historico.getAluno() == null ? null : historico.getAluno().getNaturalidade()),
                nullToEmpty(historico.getEstadoNascimento()),
                firstNonBlank(historico.getPaisNascimento(), historico.getAluno() == null ? null : historico.getAluno().getNacionalidade()),
                formatDate(historico.getDataNascimento() != null ? historico.getDataNascimento() : historico.getAluno() == null ? null : historico.getAluno().getDataNascimento()));
    }

    private List<HistoricoEscolarTelaResponse.Periodo> periodosDoHistorico(HistoricoEscolar historico) {
        Map<Integer, HistoricoEscolarTelaResponse.Periodo> periodos = new LinkedHashMap<>();
        for (HistoricoEscolarItem item : historico.getComponentesCurriculares()) {
            Integer ordem = serieOrdem(item.getSerie());
            if (ordem == null) {
                continue;
            }
            periodos.putIfAbsent(ordem, new HistoricoEscolarTelaResponse.Periodo(
                    ordem,
                    item.getAnoLetivo() == null ? "" : String.valueOf(item.getAnoLetivo()),
                    nullToEmpty(item.getSerie()),
                    ordem + "ª série"));
        }
        if (periodos.isEmpty()) {
            return periodosPadrao();
        }
        return periodos.values().stream()
                .sorted(Comparator.comparing(HistoricoEscolarTelaResponse.Periodo::ordem))
                .toList();
    }

    private List<HistoricoEscolarTelaResponse.Componente> componentesDoHistorico(
            HistoricoEscolar historico,
            List<HistoricoEscolarTelaResponse.Periodo> periodos) {
        Map<String, List<String>> linhas = new LinkedHashMap<>();
        Map<Integer, Integer> ordemParaIndice = new LinkedHashMap<>();
        for (int i = 0; i < periodos.size(); i++) {
            ordemParaIndice.put(periodos.get(i).ordem(), i);
        }

        historico.getComponentesCurriculares().stream()
                .sorted(Comparator.comparing(HistoricoEscolarItem::getComponenteCurricular, String.CASE_INSENSITIVE_ORDER))
                .forEach(item -> {
                    List<String> valores = linhas.computeIfAbsent(item.getComponenteCurricular(), key -> listaVazia(periodos.size()));
                    Integer ordem = serieOrdem(item.getSerie());
                    Integer indice = ordem == null ? null : ordemParaIndice.get(ordem);
                    if (indice != null) {
                        valores.set(indice, nullToEmpty(item.getNotaConceito()));
                    }
                });

        int ordem = 1;
        List<HistoricoEscolarTelaResponse.Componente> componentes = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : linhas.entrySet()) {
            componentes.add(new HistoricoEscolarTelaResponse.Componente(ordem++, entry.getKey(), entry.getValue()));
        }
        return componentes;
    }

    private List<String> listaVazia(int tamanho) {
        List<String> valores = new ArrayList<>();
        for (int i = 0; i < tamanho; i++) {
            valores.add("");
        }
        return valores;
    }

    private HistoricoEscolarTelaResponse.Certificado certificadoHistorico(HistoricoEscolar historico) {
        return new HistoricoEscolarTelaResponse.Certificado(
                null,
                nullToEmpty(historico.getDiretorNome()),
                nullToEmpty(historico.getNomeEscola()),
                firstNonBlank(historico.getRgRen(), historico.getAluno() == null ? null : historico.getAluno().getRg()),
                historico.getAnoConclusao() == null ? "" : String.valueOf(historico.getAnoConclusao()),
                nullToEmpty(historico.getDoeNumero()),
                formatDate(historico.getDoeData()),
                nullToEmpty(historico.getGerenteOrganizacaoNome()),
                nullToEmpty(historico.getGerenteOrganizacaoRg()),
                nullToEmpty(historico.getDiretorNome()),
                nullToEmpty(historico.getDiretorRg()));
    }

    private List<HistoricoEscolarTelaResponse.Pendencia> pendenciasHistorico(
            HistoricoEscolar historico,
            Integer serieAtual,
            Integer serieConcluidaOrigem) {
        List<HistoricoEscolarTelaResponse.Pendencia> pendencias = new ArrayList<>();
        if (historico.getComponentesCurriculares().isEmpty()) {
            pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                    "ANOS_COMPONENTES_PENDENTES",
                    "AVISO",
                    "ANOS_COMPONENTES",
                    "Há componentes curriculares ou anos letivos pendentes até a série anterior à matrícula."));
        }
        if (serieAtual != null && serieAtual > 1 && !cobreSerieAnterior(historico.getComponentesCurriculares(), serieAtual - 1)) {
            pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                    "HISTORICO_SERIE_ANTERIOR_INCOMPLETO",
                    "AVISO",
                    "ANOS_COMPONENTES",
                    "Aluno matriculado em série posterior. O histórico precisa estar preenchido até a série anterior à matrícula."));
        }
        if (serieConcluidaOrigem == null) {
            pendencias.add(new HistoricoEscolarTelaResponse.Pendencia(
                    "CERTIFICADO_PENDENTE",
                    "AVISO",
                    "OBSERVACOES_CERTIFICADO",
                    "A série concluída na escola de origem ainda precisa ser informada."));
        }
        return pendencias;
    }

    private boolean cobreSerieAnterior(List<HistoricoEscolarItem> itens, int serieObrigatoriaAte) {
        return itens.stream()
                .map(HistoricoEscolarItem::getSerie)
                .map(HistoricoEscolarServiceImpl::serieOrdem)
                .filter(java.util.Objects::nonNull)
                .anyMatch(ordem -> ordem <= serieObrigatoriaAte);
    }

    private Integer serieAtual(MatriculaEntity matricula) {
        if (matricula == null || matricula.getTurma() == null || matricula.getTurma().getSerie() == null) {
            return null;
        }
        return matricula.getTurma().getSerie().getOrdem();
    }

    private Integer serieConcluidaOrigem(TransferenciaAlunoEntity transferencia) {
        if (transferencia == null) {
            return null;
        }
        return serieOrdem(transferencia.getSerieOrigem());
    }

    private static Integer serieOrdem(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        Matcher matcher = PRIMEIRO_NUMERO.matcher(value);
        if (!matcher.find()) {
            return null;
        }
        return Integer.valueOf(matcher.group(1));
    }

    private String firstNonBlank(String primary, String fallback) {
        String value = trimToNull(primary);
        return value != null ? value : nullToEmpty(fallback);
    }

    private String blankToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private <T> T firstNonNull(T primary, T fallback) {
        return primary != null ? primary : fallback;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatDate(LocalDate value) {
        return value == null ? "" : DATE_FORMAT.format(value);
    }

    private UUID escolaId() {
        return escolaContextoPort.obterContextoPadrao().escolaId();
    }
}
