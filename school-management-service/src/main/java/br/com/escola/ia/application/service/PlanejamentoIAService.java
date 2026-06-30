package br.com.escola.ia.application.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.ia.adapter.in.web.dto.AprovarVersaoConteudoIARequest;
import br.com.escola.ia.adapter.in.web.dto.BibliotecaConteudoPedagogicoResponse;
import br.com.escola.ia.adapter.in.web.dto.ConteudoIAResponse;
import br.com.escola.ia.adapter.in.web.dto.ConteudoIAVersaoResponse;
import br.com.escola.ia.adapter.in.web.dto.CriarVersaoConteudoIARequest;
import br.com.escola.ia.adapter.in.web.dto.GerarConteudoIARequest;
import br.com.escola.ia.adapter.in.web.dto.PlanejamentoIAInteracaoResponse;
import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAConteudoGeradoEntity;
import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAConteudoVersaoEntity;
import br.com.escola.ia.adapter.out.persistence.entity.PlanejamentoIAInteracaoEntity;
import br.com.escola.ia.adapter.out.persistence.entity.StatusConteudoIAEntity;
import br.com.escola.ia.adapter.out.persistence.entity.TipoConteudoIAEntity;
import br.com.escola.ia.adapter.out.persistence.repository.PlanejamentoIAConteudoGeradoJpaRepository;
import br.com.escola.ia.adapter.out.persistence.repository.PlanejamentoIAConteudoVersaoJpaRepository;
import br.com.escola.ia.adapter.out.persistence.repository.PlanejamentoIAInteracaoJpaRepository;
import br.com.escola.ia.adapter.out.persistence.repository.StatusConteudoIAJpaRepository;
import br.com.escola.ia.adapter.out.persistence.repository.TipoConteudoIAJpaRepository;
import br.com.escola.ia.application.dto.internal.PlanejamentoIABibliotecaPublicacaoResumo;
import br.com.escola.ia.application.dto.internal.PlanejamentoIABibliotecaResumo;
import br.com.escola.ia.application.dto.internal.PlanejamentoIAResumo;
import br.com.escola.ia.application.port.internal.PlanejamentoIABibliotecaPort;
import br.com.escola.ia.application.port.internal.PlanejamentoIAPort;
import br.com.escola.ia.application.gateway.GeradorConteudoPedagogicoGateway;
import br.com.escola.ia.domain.exception.ConteudoIANaoEncontradoException;
import br.com.escola.ia.domain.exception.ConteudoIAInativoException;
import br.com.escola.ia.domain.exception.ConteudoIAPublicacaoInvalidaException;
import br.com.escola.ia.domain.exception.ConteudoIAStatusNaoEncontradoException;
import br.com.escola.ia.domain.exception.ConteudoIATipoNaoEncontradoException;
import br.com.escola.ia.domain.exception.ConteudoIAVersaoNaoEncontradaException;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.planejamento.adapter.out.persistence.entity.PlanejamentoBimestralEntity;
import br.com.escola.planejamento.domain.exception.PlanejamentoBimestralNaoEncontradoException;
import jakarta.persistence.EntityManager;

@Service
public class PlanejamentoIAService {

    private static final String STATUS_GERADO = "GERADO";
    private static final String STATUS_EM_EDICAO = "EM_EDICAO";
    private static final String STATUS_APROVADO = "APROVADO";
    private static final String ORIGEM_PLANEJAMENTO_IA = "PLANEJAMENTO_IA";

    private final PlanejamentoIAPort planejamentoIAPort;
    private final PlanejamentoIAInteracaoJpaRepository interacaoJpaRepository;
    private final PlanejamentoIAConteudoGeradoJpaRepository conteudoGeradoJpaRepository;
    private final PlanejamentoIAConteudoVersaoJpaRepository conteudoVersaoJpaRepository;
    private final PlanejamentoIABibliotecaPort planejamentoIABibliotecaPort;
    private final TipoConteudoIAJpaRepository tipoConteudoIAJpaRepository;
    private final StatusConteudoIAJpaRepository statusConteudoIAJpaRepository;
    private final GeradorConteudoPedagogicoGateway geradorConteudoPedagogicoGateway;
    private final EscolaContextoPort escolaContextoPort;
    private final EntityManager entityManager;
    private final PlanejamentoIABibliotecaFactory planejamentoIABibliotecaFactory;

    public PlanejamentoIAService(
            PlanejamentoIAPort planejamentoIAPort,
            PlanejamentoIAInteracaoJpaRepository interacaoJpaRepository,
            PlanejamentoIAConteudoGeradoJpaRepository conteudoGeradoJpaRepository,
            PlanejamentoIAConteudoVersaoJpaRepository conteudoVersaoJpaRepository,
            PlanejamentoIABibliotecaPort planejamentoIABibliotecaPort,
            TipoConteudoIAJpaRepository tipoConteudoIAJpaRepository,
            StatusConteudoIAJpaRepository statusConteudoIAJpaRepository,
            GeradorConteudoPedagogicoGateway geradorConteudoPedagogicoGateway,
            EscolaContextoPort escolaContextoPort,
            EntityManager entityManager,
            PlanejamentoIABibliotecaFactory planejamentoIABibliotecaFactory) {
        this.planejamentoIAPort = planejamentoIAPort;
        this.interacaoJpaRepository = interacaoJpaRepository;
        this.conteudoGeradoJpaRepository = conteudoGeradoJpaRepository;
        this.conteudoVersaoJpaRepository = conteudoVersaoJpaRepository;
        this.planejamentoIABibliotecaPort = planejamentoIABibliotecaPort;
        this.tipoConteudoIAJpaRepository = tipoConteudoIAJpaRepository;
        this.statusConteudoIAJpaRepository = statusConteudoIAJpaRepository;
        this.geradorConteudoPedagogicoGateway = geradorConteudoPedagogicoGateway;
        this.escolaContextoPort = escolaContextoPort;
        this.entityManager = entityManager;
        this.planejamentoIABibliotecaFactory = planejamentoIABibliotecaFactory;
    }

    @Transactional
    public ConteudoIAResponse gerarConteudo(UUID planejamentoId, GerarConteudoIARequest request) {
        var planejamento = findPlanejamentoResumo(planejamentoId);
        TipoConteudoIAEntity tipoConteudo = findTipoConteudo(request.tipoConteudo());
        StatusConteudoIAEntity statusGerado = findStatusConteudo(STATUS_GERADO);
        GeracaoConteudoPedagogicoResultado resultado = geradorConteudoPedagogicoGateway.gerar(
                new GeracaoConteudoPedagogicoComando(
                        planejamento.titulo(),
                        planejamento.temaPrincipal(),
                        planejamento.descricaoInicial(),
                        planejamento.objetivoGeral(),
                        planejamento.turmaNome(),
                        planejamento.disciplinaNome(),
                        tipoConteudo.getCodigo(),
                        request.promptProfessor()));

        PlanejamentoBimestralEntity planejamentoReference =
                entityManager.getReference(PlanejamentoBimestralEntity.class, planejamentoId);

        PlanejamentoIAInteracaoEntity interacao = interacaoJpaRepository.save(PlanejamentoIAInteracaoEntity.builder()
                .planejamentoBimestral(planejamentoReference)
                .promptProfessor(request.promptProfessor())
                .respostaIA(resultado.conteudo())
                .modeloIA(resultado.modelo())
                .tokensEntrada(resultado.tokensEntrada())
                .tokensSaida(resultado.tokensSaida())
                .custoEstimado(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build());

        String titulo = request.titulo() == null || request.titulo().isBlank()
                ? "Sugestão - " + planejamento.temaPrincipal()
                : request.titulo().trim();
        PlanejamentoIAConteudoGeradoEntity conteudo = conteudoGeradoJpaRepository.save(PlanejamentoIAConteudoGeradoEntity.builder()
                .planejamentoBimestral(planejamentoReference)
                .planejamentoIAInteracao(interacao)
                .tipoConteudoIA(tipoConteudo)
                .statusConteudoIA(statusGerado)
                .titulo(titulo)
                .conteudo(resultado.conteudo())
                .versao(1)
                .hashConteudo(sha256(resultado.conteudo()))
                .aprovadoPeloProfessor(Boolean.FALSE)
                .reutilizavel(request.reutilizavel() == null ? Boolean.TRUE : request.reutilizavel())
                .ativo(Boolean.TRUE)
                .createdAt(LocalDateTime.now())
                .build());

        conteudoVersaoJpaRepository.save(PlanejamentoIAConteudoVersaoEntity.builder()
                .planejamentoIAConteudoGerado(conteudo)
                .numeroVersao(1)
                .conteudo(resultado.conteudo())
                .motivoAlteracao("Versão inicial gerada em modo simulado.")
                .createdAt(LocalDateTime.now())
                .build());

        return toConteudoResponse(conteudo);
    }

    @Transactional(readOnly = true)
    public List<PlanejamentoIAInteracaoResponse> listarInteracoes(UUID planejamentoId) {
        validarPlanejamentoExiste(planejamentoId);
        return interacaoJpaRepository
                .findByPlanejamentoBimestral_IdAndPlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                        planejamentoId,
                        escolaId())
                .stream()
                .map(this::toInteracaoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConteudoIAResponse> listarConteudos(UUID planejamentoId) {
        validarPlanejamentoExiste(planejamentoId);
        return conteudoGeradoJpaRepository
                .findByPlanejamentoBimestral_IdAndPlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                        planejamentoId,
                        escolaId())
                .stream()
                .map(this::toConteudoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConteudoIAResponse buscarConteudo(UUID conteudoId) {
        return toConteudoResponse(findConteudo(conteudoId));
    }

    @Transactional
    public ConteudoIAVersaoResponse criarVersao(UUID conteudoId, CriarVersaoConteudoIARequest request) {
        PlanejamentoIAConteudoGeradoEntity conteudo = findConteudo(conteudoId);
        validarConteudoAtivo(conteudo);
        int proximaVersao = conteudoVersaoJpaRepository
                .findFirstByPlanejamentoIAConteudoGerado_IdAndPlanejamentoIAConteudoGerado_PlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdOrderByNumeroVersaoDesc(
                        conteudoId,
                        escolaId())
                .map(versao -> versao.getNumeroVersao() + 1)
                .orElse(1);

        PlanejamentoIAConteudoVersaoEntity versao = conteudoVersaoJpaRepository.save(PlanejamentoIAConteudoVersaoEntity.builder()
                .planejamentoIAConteudoGerado(conteudo)
                .numeroVersao(proximaVersao)
                .conteudo(request.conteudo())
                .motivoAlteracao(request.motivoAlteracao())
                .createdAt(LocalDateTime.now())
                .build());

        conteudo.setStatusConteudoIA(findStatusConteudo(STATUS_EM_EDICAO));
        conteudo.setUpdatedAt(LocalDateTime.now());
        conteudoGeradoJpaRepository.save(conteudo);

        return toVersaoResponse(versao);
    }

    @Transactional(readOnly = true)
    public List<ConteudoIAVersaoResponse> listarVersoes(UUID conteudoId) {
        if (!conteudoGeradoJpaRepository.existsByIdAndPlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                conteudoId,
                escolaId())) {
            throw new ConteudoIANaoEncontradoException();
        }
        return conteudoVersaoJpaRepository
                .findByPlanejamentoIAConteudoGerado_IdAndPlanejamentoIAConteudoGerado_PlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                        conteudoId,
                        escolaId())
                .stream()
                .map(this::toVersaoResponse)
                .toList();
    }

    @Transactional
    public ConteudoIAResponse aprovarVersao(UUID conteudoId, AprovarVersaoConteudoIARequest request) {
        PlanejamentoIAConteudoGeradoEntity conteudo = findConteudo(conteudoId);
        validarConteudoAtivo(conteudo);
        PlanejamentoIAConteudoVersaoEntity versao = conteudoVersaoJpaRepository
                .findByPlanejamentoIAConteudoGerado_IdAndPlanejamentoIAConteudoGerado_PlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_IdAndNumeroVersao(
                        conteudoId,
                        escolaId(),
                        request.numeroVersao())
                .orElseThrow(ConteudoIAVersaoNaoEncontradaException::new);

        conteudo.setConteudo(versao.getConteudo());
        conteudo.setVersao(versao.getNumeroVersao());
        conteudo.setHashConteudo(sha256(versao.getConteudo()));
        conteudo.setAprovadoPeloProfessor(Boolean.TRUE);
        conteudo.setStatusConteudoIA(findStatusConteudo(STATUS_APROVADO));
        conteudo.setUpdatedAt(LocalDateTime.now());

        PlanejamentoIAConteudoGeradoEntity salvo = conteudoGeradoJpaRepository.save(conteudo);
        if (Boolean.TRUE.equals(request.publicarBiblioteca())) {
            publicarBibliotecaResumo(salvo);
        }
        return toConteudoResponse(salvo);
    }

    @Transactional
    public BibliotecaConteudoPedagogicoResponse publicarBiblioteca(UUID conteudoId) {
        return toBibliotecaResponse(publicarBibliotecaResumo(findConteudo(conteudoId)));
    }

    @Transactional(readOnly = true)
    public List<BibliotecaConteudoPedagogicoResponse> listarBiblioteca(
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema) {
        String tipoNormalizado = normalizarTipoConteudoExistente(tipoConteudo);
        String temaNormalizado = tema == null || tema.isBlank() ? null : tema.trim();
        return planejamentoIABibliotecaPort
                .listar(professorId, disciplinaId, tipoNormalizado, temaNormalizado, escolaId())
                .stream()
                .map(this::toBibliotecaResponse)
                .toList();
    }

    private PlanejamentoIABibliotecaResumo publicarBibliotecaResumo(PlanejamentoIAConteudoGeradoEntity conteudo) {
        validarConteudoAtivo(conteudo);
        if (!Boolean.TRUE.equals(conteudo.getAprovadoPeloProfessor())) {
            throw new ConteudoIAPublicacaoInvalidaException();
        }

        PlanejamentoIABibliotecaPublicacaoResumo resumo =
                planejamentoIABibliotecaFactory.extrairResumo(conteudo, ORIGEM_PLANEJAMENTO_IA);
        return planejamentoIABibliotecaPort.publicar(resumo);
    }

    private PlanejamentoIAResumo findPlanejamentoResumo(UUID planejamentoId) {
        return planejamentoIAPort.buscarResumo(planejamentoId, escolaId())
                .orElseThrow(PlanejamentoBimestralNaoEncontradoException::new);
    }

    private void validarPlanejamentoExiste(UUID planejamentoId) {
        if (!planejamentoIAPort.existePlanejamento(planejamentoId, escolaId())) {
            throw new PlanejamentoBimestralNaoEncontradoException();
        }
    }

    private PlanejamentoIAConteudoGeradoEntity findConteudo(UUID conteudoId) {
        return conteudoGeradoJpaRepository
                .findByIdAndPlanejamentoBimestral_ProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                        conteudoId,
                        escolaId())
                .orElseThrow(ConteudoIANaoEncontradoException::new);
    }

    private void validarConteudoAtivo(PlanejamentoIAConteudoGeradoEntity conteudo) {
        if (!Boolean.TRUE.equals(conteudo.getAtivo())) {
            throw new ConteudoIAInativoException();
        }
    }

    private TipoConteudoIAEntity findTipoConteudo(String codigo) {
        String codigoNormalizado = codigo.trim().toUpperCase(Locale.ROOT);
        return tipoConteudoIAJpaRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new ConteudoIATipoNaoEncontradoException(codigoNormalizado));
    }

    private String normalizarTipoConteudoExistente(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return findTipoConteudo(codigo).getCodigo();
    }

    private StatusConteudoIAEntity findStatusConteudo(String codigo) {
        return statusConteudoIAJpaRepository.findByCodigo(codigo)
                .orElseThrow(ConteudoIAStatusNaoEncontradoException::new);
    }

    private PlanejamentoIAInteracaoResponse toInteracaoResponse(PlanejamentoIAInteracaoEntity entity) {
        var contextoEscola = escolaContextoPort.obterContextoPadrao();
        return new PlanejamentoIAInteracaoResponse(
                entity.getId(),
                entity.getPlanejamentoBimestral().getId(),
                contextoEscola.escolaId(),
                contextoEscola.escolaNome(),
                entity.getPromptProfessor(),
                entity.getRespostaIA(),
                entity.getModeloIA(),
                entity.getTokensEntrada(),
                entity.getTokensSaida(),
                entity.getCustoEstimado(),
                entity.getCreatedAt());
    }

    private ConteudoIAResponse toConteudoResponse(PlanejamentoIAConteudoGeradoEntity entity) {
        var contextoEscola = escolaContextoPort.obterContextoPadrao();
        return new ConteudoIAResponse(
                entity.getId(),
                entity.getPlanejamentoBimestral().getId(),
                entity.getPlanejamentoIAInteracao() == null ? null : entity.getPlanejamentoIAInteracao().getId(),
                contextoEscola.escolaId(),
                contextoEscola.escolaNome(),
                entity.getTitulo(),
                entity.getConteudo(),
                entity.getVersao(),
                entity.getHashConteudo(),
                entity.getAprovadoPeloProfessor(),
                entity.getReutilizavel(),
                entity.getAtivo(),
                entity.getStatusConteudoIA() == null ? null : entity.getStatusConteudoIA().getCodigo(),
                entity.getStatusConteudoIA() == null ? null : entity.getStatusConteudoIA().getDescricao(),
                entity.getTipoConteudoIA().getCodigo(),
                entity.getTipoConteudoIA().getDescricao(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private ConteudoIAVersaoResponse toVersaoResponse(PlanejamentoIAConteudoVersaoEntity entity) {
        return new ConteudoIAVersaoResponse(
                entity.getId(),
                entity.getPlanejamentoIAConteudoGerado().getId(),
                entity.getNumeroVersao(),
                entity.getConteudo(),
                entity.getMotivoAlteracao(),
                entity.getCreatedAt());
    }

    private BibliotecaConteudoPedagogicoResponse toBibliotecaResponse(PlanejamentoIABibliotecaResumo resumo) {
        return new BibliotecaConteudoPedagogicoResponse(
                resumo.id(),
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.professorId(),
                resumo.professorNome(),
                resumo.disciplinaId(),
                resumo.disciplinaNome(),
                resumo.tipoConteudo(),
                resumo.tipoConteudoDescricao(),
                resumo.titulo(),
                resumo.tema(),
                resumo.conteudo(),
                resumo.origem(),
                resumo.reutilizavel(),
                resumo.ativo(),
                resumo.createdAt(),
                resumo.updatedAt());
    }

    private String sha256(String conteudo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(conteudo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível.", ex);
        }
    }

    private UUID escolaId() {
        return escolaContextoPort.obterContextoPadrao().escolaId();
    }
}
