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

import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarGeracaoRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarItemRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarResponse;
import br.com.escola.historico.adapter.out.persistence.entity.BoletimEntity;
import br.com.escola.historico.adapter.out.persistence.entity.BoletimItemEntity;
import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolar;
import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolarItem;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimItemJpaRepository;
import br.com.escola.historico.adapter.out.persistence.repository.BoletimJpaRepository;
import br.com.escola.historico.adapter.out.persistence.repository.HistoricoEscolarJpaRepository;
import br.com.escola.historico.application.mapper.HistoricoEscolarMapper;
import br.com.escola.historico.domain.exception.BoletimFechadoNaoEncontradoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarDuplicadoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarInvalidoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarNaoEncontradoException;
import br.com.escola.matricula.domain.exception.MatriculaNaoEncontradaException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoricoEscolarServiceImpl implements HistoricoEscolarService {

    private final HistoricoEscolarJpaRepository historicoEscolarJpaRepository;
    private final BoletimJpaRepository boletimJpaRepository;
    private final BoletimItemJpaRepository boletimItemJpaRepository;
    private final HistoricoEscolarMapper historicoEscolarMapper;

    @Override
    @Transactional
    public HistoricoEscolarResponse criar(HistoricoEscolarRequest request) {
        validarRequest(request);
        var historico = historicoEscolarMapper.toEntity(request);
        return historicoEscolarMapper.toResponse(historicoEscolarJpaRepository.save(historico));
    }

    @Override
    @Transactional
    public HistoricoEscolarResponse atualizar(UUID id, HistoricoEscolarRequest request) {
        validarRequest(request);
        var historico = historicoEscolarJpaRepository.findWithComponentesCurricularesById(id)
                .orElseThrow(() -> new HistoricoEscolarNaoEncontradoException(id));
        historicoEscolarMapper.copyToEntity(request, historico);
        return historicoEscolarMapper.toResponse(historicoEscolarJpaRepository.save(historico));
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        if (!historicoEscolarJpaRepository.existsById(id)) {
            throw new HistoricoEscolarNaoEncontradoException(id);
        }
        historicoEscolarJpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoricoEscolarResponse buscarPorId(UUID id) {
        return historicoEscolarJpaRepository.findWithComponentesCurricularesById(id)
                .map(historicoEscolarMapper::toResponse)
                .orElseThrow(() -> new HistoricoEscolarNaoEncontradoException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoricoEscolarResponse> listar(Pageable pageable) {
        return historicoEscolarJpaRepository.findAll(pageable)
                .map(historicoEscolarMapper::toResponse);
    }

    @Override
    @Transactional
    public HistoricoEscolarResponse gerarPorBoletim(UUID matriculaId, HistoricoEscolarGeracaoRequest request) {
        BoletimEntity boletim = boletimJpaRepository.findById(request.boletimId())
                .orElseThrow(() -> new BoletimFechadoNaoEncontradoException(request.boletimId()));
        if (!boletim.getMatricula().getId().equals(matriculaId)) {
            throw new MatriculaNaoEncontradaException(matriculaId);
        }

        var matricula = boletim.getMatricula();
        UUID alunoId = matricula.getAluno().getId();
        UUID periodoLetivoId = matricula.getPeriodoLetivo().getId();

        List<HistoricoEscolar> duplicados = historicoEscolarJpaRepository
                .findByAlunoIdAndPeriodoLetivoId(alunoId, periodoLetivoId);
        if (!duplicados.isEmpty() && !Boolean.TRUE.equals(request.sobrescrever())) {
            throw new HistoricoEscolarDuplicadoException(alunoId, periodoLetivoId);
        }
        duplicados.forEach(historicoEscolarJpaRepository::delete);

        List<BoletimItemEntity> boletimItens = boletimItemJpaRepository.findByBoletimId(boletim.getId());
        if (boletimItens.isEmpty()) {
            throw new HistoricoEscolarInvalidoException("Boletim fechado não possui itens para geração do histórico escolar");
        }

        HistoricoEscolar historico = HistoricoEscolar.builder()
                .alunoId(alunoId)
                .origem("INTERNO")
                .nomeAluno(matricula.getAluno().getNomeCompleto())
                .rgRen(matricula.getAluno().getRg())
                .ra(matricula.getAluno().getRa())
                .rm(matricula.getAluno().getRm())
                .dataNascimento(matricula.getAluno().getDataNascimento())
                .municipioNascimento(matricula.getAluno().getNaturalidade())
                .paisNascimento(matricula.getAluno().getNacionalidade())
                .anoConclusao(matricula.getPeriodoLetivo().getAno())
                .ensinoConcluido(trimToNull(request.ensinoConcluido()))
                .dataEmissao(LocalDate.now())
                .observacoes(observacoesGeracao(request, boletim))
                .build();

        boletimItens.stream()
                .sorted((a, b) -> a.getDisciplina().getNome().compareToIgnoreCase(b.getDisciplina().getNome()))
                .map(item -> toHistoricoItem(boletim, item))
                .forEach(historico::addComponenteCurricular);

        return historicoEscolarMapper.toResponse(historicoEscolarJpaRepository.save(historico));
    }

    private HistoricoEscolarItem toHistoricoItem(BoletimEntity boletim, BoletimItemEntity item) {
        return HistoricoEscolarItem.builder()
                .periodoLetivo(boletim.getMatricula().getPeriodoLetivo())
                .serieEntity(boletim.getMatricula().getTurma().getSerie())
                .disciplina(item.getDisciplina())
                .componenteCurricular(item.getDisciplina().getNome())
                .anoLetivo(boletim.getMatricula().getPeriodoLetivo().getAno())
                .serie(boletim.getMatricula().getTurma().getSerie().getNome())
                .notaConceito(toNotaConceito(item.getMedia()))
                .frequenciaPercentual(item.getFrequenciaPercentual())
                .totalAulas(item.getCargaHoraria())
                .cargaHoraria(item.getDisciplina().getCargaHoraria())
                .resultado(item.getResultado())
                .build();
    }

    private String toNotaConceito(BigDecimal media) {
        if (media == null) {
            return null;
        }
        return media.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String observacoesGeracao(HistoricoEscolarGeracaoRequest request, BoletimEntity boletim) {
        String observacoes = trimToNull(request.observacoes());
        if (observacoes != null) {
            return observacoes;
        }
        return "Histórico gerado a partir do boletim fechado %s, período %s"
                .formatted(boletim.getId(), boletim.getPeriodoReferencia());
    }

    private void validarRequest(HistoricoEscolarRequest request) {
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
}
