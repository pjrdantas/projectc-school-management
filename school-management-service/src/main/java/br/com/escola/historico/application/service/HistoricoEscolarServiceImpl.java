package br.com.escola.historico.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

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
import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolar;
import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolarItem;
import br.com.escola.historico.adapter.out.persistence.repository.HistoricoEscolarJpaRepository;
import br.com.escola.historico.application.dto.internal.BoletimHistoricoItemResumo;
import br.com.escola.historico.application.port.internal.BoletimHistoricoPort;
import br.com.escola.historico.application.mapper.HistoricoEscolarMapper;
import br.com.escola.historico.domain.exception.BoletimFechadoNaoEncontradoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarDuplicadoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarInvalidoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarNaoEncontradoException;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoricoEscolarServiceImpl implements HistoricoEscolarService {

    private final HistoricoEscolarJpaRepository historicoEscolarJpaRepository;
    private final BoletimHistoricoPort boletimHistoricoPort;
    private final HistoricoEscolarMapper historicoEscolarMapper;
    private final AlunoMatriculaPort alunoMatriculaPort;
    private final EscolaContextoPort escolaContextoPort;

    @Override
    @Transactional
    public HistoricoEscolarResponse criar(HistoricoEscolarRequest request) {
        validarRequest(request);
        var historico = historicoEscolarMapper.toEntity(request);
        historico.setAluno(alunoEscopado(request.alunoId()));
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
        Integer anoConclusao = boletim.anoConclusao();

        List<HistoricoEscolar> duplicados = historicoEscolarJpaRepository
                .findByAlunoIdAndEscolaIdAndPeriodoLetivoId(alunoId, escolaId, boletim.periodoLetivoId());
        if (!duplicados.isEmpty() && !Boolean.TRUE.equals(request.sobrescrever())) {
            throw new HistoricoEscolarDuplicadoException(alunoId, boletim.periodoLetivoId());
        }
        duplicados.forEach(historicoEscolarJpaRepository::delete);

        if (boletim.itens().isEmpty()) {
            throw new HistoricoEscolarInvalidoException("Boletim fechado não possui itens para geração do histórico escolar");
        }

        HistoricoEscolar historico = HistoricoEscolar.builder()
                .alunoId(alunoId)
                .origem("INTERNO")
                .nomeAluno(boletim.nomeAluno())
                .rgRen(boletim.rg())
                .ra(boletim.ra())
                .rm(boletim.rm())
                .dataNascimento(boletim.dataNascimento())
                .municipioNascimento(boletim.naturalidade())
                .paisNascimento(boletim.nacionalidade())
                .anoConclusao(anoConclusao)
                .ensinoConcluido(trimToNull(request.ensinoConcluido()))
                .dataEmissao(LocalDate.now())
                .observacoes(observacoesGeracao(request, boletim))
                .build();
        historico.setAluno(alunoEscopado(alunoId));

        boletim.itens().stream()
                .map(this::toHistoricoItem)
                .forEach(historico::addComponenteCurricular);

        var salvo = historicoEscolarJpaRepository.save(historico);
        return historicoEscolarMapper.toResponse(buscarHistoricoEscopado(salvo.getId()));
    }

    private HistoricoEscolarItem toHistoricoItem(BoletimHistoricoItemResumo item) {
        return HistoricoEscolarItem.builder()
                .periodoLetivo(item.periodoLetivo())
                .serieEntity(item.serieEntity())
                .disciplina(item.disciplina())
                .componenteCurricular(item.componenteCurricular())
                .anoLetivo(item.anoLetivo())
                .serie(item.serie())
                .notaConceito(toNotaConceito(item.media()))
                .frequenciaPercentual(item.frequenciaPercentual())
                .totalAulas(item.totalAulas())
                .cargaHoraria(item.cargaHoraria())
                .resultado(item.resultado())
                .build();
    }

    private String toNotaConceito(BigDecimal media) {
        if (media == null) {
            return null;
        }
        return media.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String observacoesGeracao(HistoricoEscolarGeracaoRequest request, br.com.escola.historico.application.dto.internal.BoletimHistoricoResumo boletim) {
        String observacoes = trimToNull(request.observacoes());
        if (observacoes != null) {
            return observacoes;
        }
        return "Histórico gerado a partir do boletim fechado %s, período %s"
                .formatted(boletim.boletimId(), boletim.periodoReferencia());
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

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private HistoricoEscolar buscarHistoricoEscopado(UUID id) {
        return historicoEscolarJpaRepository.findWithComponentesCurricularesByIdAndEscolaId(id, escolaId())
                .orElseThrow(() -> new HistoricoEscolarNaoEncontradoException(id));
    }

    private AlunoEntity alunoEscopado(UUID alunoId) {
        return alunoMatriculaPort.buscarAlunoPorIdEEscola(alunoId, escolaId())
                .orElseThrow(() -> new HistoricoEscolarInvalidoException("Aluno do histórico escolar não encontrado"));
    }

    private UUID escolaId() {
        return escolaContextoPort.obterContextoPadrao().escolaId();
    }
}
