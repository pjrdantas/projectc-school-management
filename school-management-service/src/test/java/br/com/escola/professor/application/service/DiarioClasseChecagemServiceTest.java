package br.com.escola.professor.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.escola.professor.adapter.out.persistence.entity.DiarioClasseLancamentoEntity;
import br.com.escola.professor.adapter.out.persistence.repository.DiarioClasseLancamentoJpaRepository;
import br.com.escola.professor.application.dto.internal.AutoridadePedagogicaResumo;
import br.com.escola.professor.application.port.internal.AutoridadePedagogicaPort;
import br.com.escola.professor.domain.exception.DiarioClasseLancamentoInvalidoException;

@ExtendWith(MockitoExtension.class)
class DiarioClasseChecagemServiceTest {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 7, 1, 10, 15);

    @Mock
    private AutoridadePedagogicaPort autoridadePedagogicaPort;

    @Mock
    private DiarioClasseLancamentoJpaRepository diarioClasseLancamentoRepository;

    private DiarioClasseChecagemService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AGORA.atZone(ZONE_ID).toInstant(), ZONE_ID);
        service = new DiarioClasseChecagemService(
                autoridadePedagogicaPort,
                diarioClasseLancamentoRepository,
                clock);
    }

    @Test
    void deveChecarCoordenacaoQuandoDiarioEstiverBloqueadoPeloProfessor() {
        UUID lancamentoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();
        DiarioClasseLancamentoEntity lancamento = lancamento(lancamentoId, "BLOQUEADO");
        AutoridadePedagogicaResumo autoridade = autoridade(escolaId, funcionarioId, "COORDENADOR");

        when(autoridadePedagogicaPort.resolver("access-token", Set.of("COORDENADOR"))).thenReturn(autoridade);
        when(diarioClasseLancamentoRepository.findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                lancamentoId,
                escolaId)).thenReturn(Optional.of(lancamento));
        when(diarioClasseLancamentoRepository.save(lancamento)).thenReturn(lancamento);

        var resumo = service.checarCoordenacao("access-token", lancamentoId, " Conferido sem pendencias ");

        assertThat(resumo.status()).isEqualTo("CHECADO_COORDENACAO");
        assertThat(resumo.checadoCoordenacaoPorFuncionario()).isEqualTo(funcionarioId);
        assertThat(resumo.checadoCoordenacaoEm()).isEqualTo(AGORA);
        assertThat(lancamento.getObservacaoCoordenacao()).isEqualTo("Conferido sem pendencias");
        assertThat(lancamento.getUpdatedAt()).isEqualTo(AGORA);
        verify(diarioClasseLancamentoRepository).save(lancamento);
    }

    @Test
    void deveChecarDirecaoAposChecagemDaCoordenacao() {
        UUID lancamentoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID coordenadorId = UUID.randomUUID();
        UUID diretorId = UUID.randomUUID();
        DiarioClasseLancamentoEntity lancamento = lancamento(lancamentoId, "CHECADO_COORDENACAO");
        lancamento.setChecadoCoordenacaoPorFuncionario(coordenadorId);
        lancamento.setChecadoCoordenacaoEm(AGORA.minusHours(1));
        AutoridadePedagogicaResumo autoridade = autoridade(escolaId, diretorId, "DIRETOR");

        when(autoridadePedagogicaPort.resolver("access-token", Set.of("DIRETOR"))).thenReturn(autoridade);
        when(diarioClasseLancamentoRepository.findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                lancamentoId,
                escolaId)).thenReturn(Optional.of(lancamento));
        when(diarioClasseLancamentoRepository.save(lancamento)).thenReturn(lancamento);

        var resumo = service.checarDirecao("access-token", lancamentoId, "Validado pela direcao");

        assertThat(resumo.status()).isEqualTo("CHECADO_DIRECAO");
        assertThat(resumo.checadoDirecaoPorFuncionario()).isEqualTo(diretorId);
        assertThat(resumo.checadoDirecaoEm()).isEqualTo(AGORA);
        assertThat(lancamento.getObservacaoDirecao()).isEqualTo("Validado pela direcao");
        verify(diarioClasseLancamentoRepository).save(lancamento);
    }

    @Test
    void deveRejeitarChecagemDaCoordenacaoQuandoDiarioNaoEstiverBloqueado() {
        UUID lancamentoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        DiarioClasseLancamentoEntity lancamento = lancamento(lancamentoId, "SALVO");
        AutoridadePedagogicaResumo autoridade = autoridade(escolaId, UUID.randomUUID(), "COORDENADOR");

        when(autoridadePedagogicaPort.resolver("access-token", Set.of("COORDENADOR"))).thenReturn(autoridade);
        when(diarioClasseLancamentoRepository.findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                lancamentoId,
                escolaId)).thenReturn(Optional.of(lancamento));

        assertThatThrownBy(() -> service.checarCoordenacao("access-token", lancamentoId, null))
                .isInstanceOf(DiarioClasseLancamentoInvalidoException.class)
                .hasMessageContaining("bloqueado pelo professor");
    }

    @Test
    void deveRejeitarChecagemDaDirecaoAntesDaCoordenacao() {
        UUID lancamentoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        DiarioClasseLancamentoEntity lancamento = lancamento(lancamentoId, "BLOQUEADO");
        AutoridadePedagogicaResumo autoridade = autoridade(escolaId, UUID.randomUUID(), "DIRETOR");

        when(autoridadePedagogicaPort.resolver("access-token", Set.of("DIRETOR"))).thenReturn(autoridade);
        when(diarioClasseLancamentoRepository.findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                lancamentoId,
                escolaId)).thenReturn(Optional.of(lancamento));

        assertThatThrownBy(() -> service.checarDirecao("access-token", lancamentoId, null))
                .isInstanceOf(DiarioClasseLancamentoInvalidoException.class)
                .hasMessageContaining("coordenacao antes");
    }

    @Test
    void deveRejeitarObservacaoAcimaDoLimite() {
        UUID lancamentoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        DiarioClasseLancamentoEntity lancamento = lancamento(lancamentoId, "BLOQUEADO");
        AutoridadePedagogicaResumo autoridade = autoridade(escolaId, UUID.randomUUID(), "COORDENADOR");

        when(autoridadePedagogicaPort.resolver("access-token", Set.of("COORDENADOR"))).thenReturn(autoridade);
        when(diarioClasseLancamentoRepository.findByIdAndProfessorTurmaDisciplina_TurmaDisciplina_Turma_Escola_Id(
                lancamentoId,
                escolaId)).thenReturn(Optional.of(lancamento));

        assertThatThrownBy(() -> service.checarCoordenacao("access-token", lancamentoId, "a".repeat(501)))
                .isInstanceOf(DiarioClasseLancamentoInvalidoException.class)
                .hasMessageContaining("500 caracteres");
    }

    private DiarioClasseLancamentoEntity lancamento(UUID id, String status) {
        return DiarioClasseLancamentoEntity.builder()
                .id(id)
                .status(status)
                .bloqueado(Boolean.TRUE)
                .createdAt(LocalDateTime.ofInstant(Instant.parse("2026-07-01T12:00:00Z"), ZoneId.of("UTC")))
                .build();
    }

    private AutoridadePedagogicaResumo autoridade(UUID escolaId, UUID funcionarioId, String cargoCodigo) {
        return new AutoridadePedagogicaResumo(
                UUID.randomUUID(),
                escolaId,
                funcionarioId,
                UUID.randomUUID(),
                "Autoridade Pedagogica",
                cargoCodigo,
                cargoCodigo,
                List.of("PEDAGOGICO"),
                List.of("DIARIO_CHECAGEM"));
    }
}
