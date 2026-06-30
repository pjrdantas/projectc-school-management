package br.com.escola.dashboard.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.escola.avaliacao.adapter.out.persistence.repository.AvaliacaoJpaRepository;
import br.com.escola.avaliacao.adapter.out.persistence.repository.NotaAlunoJpaRepository;
import br.com.escola.frequencia.adapter.out.persistence.repository.FrequenciaProfessorJpaRepository;
import br.com.escola.institucional.application.dto.EscolaContexto;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.planejamento.adapter.out.persistence.repository.PlanejamentoBimestralJpaRepository;
import br.com.escola.professor.adapter.out.persistence.repository.AulaJpaRepository;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorJpaRepository;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorTurmaDisciplinaJpaRepository;
import br.com.escola.professor.domain.exception.ProfessorNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class DashboardProfessorServiceTest {

    @Mock
    private ProfessorJpaRepository professorJpaRepository;

    @Mock
    private ProfessorTurmaDisciplinaJpaRepository professorTurmaDisciplinaJpaRepository;

    @Mock
    private AulaJpaRepository aulaJpaRepository;

    @Mock
    private FrequenciaProfessorJpaRepository frequenciaProfessorJpaRepository;

    @Mock
    private AvaliacaoJpaRepository avaliacaoJpaRepository;

    @Mock
    private NotaAlunoJpaRepository notaAlunoJpaRepository;

    @Mock
    private PlanejamentoBimestralJpaRepository planejamentoBimestralJpaRepository;

    @Mock
    private EscolaContextoPort escolaContextoPort;

    @Test
    void deveLancarErroQuandoProfessorNaoPertenceAoContextoDaEscola() {
        UUID professorId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        when(escolaContextoPort.obterContextoPadrao()).thenReturn(contexto(escolaId));
        when(professorJpaRepository.existsByIdAndPessoa_Escola_Id(professorId, escolaId)).thenReturn(false);

        assertThatThrownBy(() -> service().consultar(professorId))
                .isInstanceOf(ProfessorNaoEncontradoException.class)
                .hasMessageContaining("Professor não encontrado");

        verify(professorJpaRepository).existsByIdAndPessoa_Escola_Id(professorId, escolaId);
        verifyNoInteractions(professorTurmaDisciplinaJpaRepository);
    }

    @Test
    void deveRetornarResumoZeradoQuandoProfessorNaoPossuiAlocacoesNaEscola() {
        UUID professorId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        when(escolaContextoPort.obterContextoPadrao()).thenReturn(contexto(escolaId));
        when(professorJpaRepository.existsByIdAndPessoa_Escola_Id(professorId, escolaId)).thenReturn(true);
        when(professorTurmaDisciplinaJpaRepository.findByProfessorId(professorId)).thenReturn(List.of());

        var response = service().consultar(professorId);

        assertThat(response.escolaId()).isEqualTo(escolaId);
        assertThat(response.escolaNome()).isEqualTo("Escola teste");
        assertThat(response.professorId()).isEqualTo(professorId);
        assertThat(response.turmasVinculadas()).isZero();
        assertThat(response.alocacoesAtivas()).isZero();
        assertThat(response.aulasPlanejadas()).isZero();
        assertThat(response.aulasRealizadas()).isZero();
        assertThat(response.frequenciasPendentes()).isZero();
        assertThat(response.avaliacoesRegistradas()).isZero();
        assertThat(response.avaliacoesComNotasPendentes()).isZero();
        assertThat(response.planejamentosBimestrais()).isZero();
        assertThat(response.planejamentosBimestraisPendentes()).isZero();
        assertThat(response.turmas()).isEmpty();
        verify(professorTurmaDisciplinaJpaRepository).findByProfessorId(professorId);
    }

    private DashboardProfessorService service() {
        return new DashboardProfessorService(
                professorJpaRepository,
                professorTurmaDisciplinaJpaRepository,
                aulaJpaRepository,
                frequenciaProfessorJpaRepository,
                avaliacaoJpaRepository,
                notaAlunoJpaRepository,
                planejamentoBimestralJpaRepository,
                escolaContextoPort);
    }

    private EscolaContexto contexto(UUID escolaId) {
        return new EscolaContexto(escolaId, "Escola teste", UUID.randomUUID(), Set.of(), Set.of());
    }
}
