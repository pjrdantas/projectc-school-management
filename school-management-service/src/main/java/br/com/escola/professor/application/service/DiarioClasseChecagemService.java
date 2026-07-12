package br.com.escola.professor.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.professor.adapter.out.persistence.entity.DiarioClasseLancamentoEntity;
import br.com.escola.professor.adapter.out.persistence.repository.DiarioClasseLancamentoJpaRepository;
import br.com.escola.professor.application.dto.internal.AutoridadePedagogicaResumo;
import br.com.escola.professor.application.dto.internal.DiarioClasseChecagemResumo;
import br.com.escola.professor.application.port.internal.AutoridadePedagogicaPort;
import br.com.escola.professor.domain.exception.DiarioClasseLancamentoInvalidoException;
import br.com.escola.professor.domain.exception.ProfessorTurmaDisciplinaNaoEncontradaException;

@Service
public class DiarioClasseChecagemService {

    private static final String STATUS_BLOQUEADO = "BLOQUEADO";
    private static final String STATUS_CHECADO_COORDENACAO = "CHECADO_COORDENACAO";
    private static final String STATUS_CHECADO_DIRECAO = "CHECADO_DIRECAO";
    private static final int LIMITE_OBSERVACAO = 500;

    private final AutoridadePedagogicaPort autoridadePedagogicaPort;
    private final DiarioClasseLancamentoJpaRepository diarioClasseLancamentoRepository;
    private final Clock clock;

    public DiarioClasseChecagemService(
            AutoridadePedagogicaPort autoridadePedagogicaPort,
            DiarioClasseLancamentoJpaRepository diarioClasseLancamentoRepository,
            Clock clock) {
        this.autoridadePedagogicaPort = autoridadePedagogicaPort;
        this.diarioClasseLancamentoRepository = diarioClasseLancamentoRepository;
        this.clock = clock;
    }

    @Transactional
    public DiarioClasseChecagemResumo checarCoordenacao(String accessToken, UUID diarioClasseLancamentoId, String observacao) {
        AutoridadePedagogicaResumo autoridade = autoridadePedagogicaPort.resolver(accessToken, Set.of("COORDENADOR"));
        DiarioClasseLancamentoEntity lancamento = findLancamento(diarioClasseLancamentoId, autoridade);

        if (!STATUS_BLOQUEADO.equals(lancamento.getStatus())) {
            throw new DiarioClasseLancamentoInvalidoException(
                    "Diario de classe deve estar bloqueado pelo professor antes da checagem da coordenacao.");
        }
        if (lancamento.getChecadoCoordenacaoPorFuncionario() != null) {
            throw new DiarioClasseLancamentoInvalidoException("Diario de classe ja checado pela coordenacao.");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        lancamento.setStatus(STATUS_CHECADO_COORDENACAO);
        lancamento.setChecadoCoordenacaoPorFuncionario(autoridade.funcionarioId());
        lancamento.setChecadoCoordenacaoEm(agora);
        lancamento.setObservacaoCoordenacao(normalizarObservacao(observacao));
        lancamento.setUpdatedAt(agora);

        return toResumo(diarioClasseLancamentoRepository.save(lancamento), autoridade.escolaId());
    }

    @Transactional
    public DiarioClasseChecagemResumo checarDirecao(String accessToken, UUID diarioClasseLancamentoId, String observacao) {
        AutoridadePedagogicaResumo autoridade = autoridadePedagogicaPort.resolver(accessToken, Set.of("DIRETOR"));
        DiarioClasseLancamentoEntity lancamento = findLancamento(diarioClasseLancamentoId, autoridade);

        if (!STATUS_CHECADO_COORDENACAO.equals(lancamento.getStatus())
                || lancamento.getChecadoCoordenacaoPorFuncionario() == null
                || lancamento.getChecadoCoordenacaoEm() == null) {
            throw new DiarioClasseLancamentoInvalidoException(
                    "Diario de classe deve estar checado pela coordenacao antes da checagem da direcao.");
        }
        if (lancamento.getChecadoDirecaoPorFuncionario() != null) {
            throw new DiarioClasseLancamentoInvalidoException("Diario de classe ja checado pela direcao.");
        }

        LocalDateTime agora = LocalDateTime.now(clock);
        lancamento.setStatus(STATUS_CHECADO_DIRECAO);
        lancamento.setChecadoDirecaoPorFuncionario(autoridade.funcionarioId());
        lancamento.setChecadoDirecaoEm(agora);
        lancamento.setObservacaoDirecao(normalizarObservacao(observacao));
        lancamento.setUpdatedAt(agora);

        return toResumo(diarioClasseLancamentoRepository.save(lancamento), autoridade.escolaId());
    }

    private DiarioClasseLancamentoEntity findLancamento(
            UUID diarioClasseLancamentoId,
            AutoridadePedagogicaResumo autoridade) {
        if (diarioClasseLancamentoId == null) {
            throw new DiarioClasseLancamentoInvalidoException("Identificador do lancamento do diario e obrigatorio.");
        }
        return diarioClasseLancamentoRepository
                .findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                        diarioClasseLancamentoId,
                        autoridade.escolaId())
                .orElseThrow(ProfessorTurmaDisciplinaNaoEncontradaException::new);
    }

    private String normalizarObservacao(String observacao) {
        if (observacao == null || observacao.isBlank()) {
            return null;
        }
        String normalizada = observacao.trim();
        if (normalizada.length() > LIMITE_OBSERVACAO) {
            throw new DiarioClasseLancamentoInvalidoException(
                    "Observacao da checagem deve ter no maximo 500 caracteres.");
        }
        return normalizada;
    }

    private DiarioClasseChecagemResumo toResumo(DiarioClasseLancamentoEntity lancamento, UUID escolaId) {
        return new DiarioClasseChecagemResumo(
                lancamento.getId(),
                escolaId,
                lancamento.getStatus(),
                lancamento.getChecadoCoordenacaoPorFuncionario(),
                lancamento.getChecadoCoordenacaoEm(),
                lancamento.getChecadoDirecaoPorFuncionario(),
                lancamento.getChecadoDirecaoEm(),
                lancamento.getBloqueado());
    }
}
