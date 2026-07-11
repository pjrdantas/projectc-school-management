package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.dto.PessoaProfessorInternalSummaryResponse;
import br.com.escola.peopleservice.application.port.out.PeopleProfessorInternalSummaryPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleProfessorInternalSummaryServiceTest {

    @Test
    void retornaVazioQuandoAdapterNaoExiste() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleProfessorInternalSummaryService service =
                new PeopleProfessorInternalSummaryService(provider(null), meterRegistry);

        Optional<PessoaProfessorInternalSummaryResponse> response =
                service.buscarProfessorPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.professor.internal.summary.reads",
                "operation", "buscarProfessorPorId",
                "result", "fallback_adapter_missing").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaProfessorInternalSummaryResponse professor = professor();
        CountingProfessorInternalSummaryPort port = new CountingProfessorInternalSummaryPort(professor);
        PeopleProfessorInternalSummaryService service =
                new PeopleProfessorInternalSummaryService(provider(port), meterRegistry);

        List<PessoaProfessorInternalSummaryResponse> response =
                service.listarProfessoresPorEscola(UUID.randomUUID());

        assertThat(response).containsExactly(professor);
        assertThat(port.listCalls).isEqualTo(1);
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.professor.internal.summary.reads",
                "operation", "listarProfessoresPorEscola",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaListaVaziaQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleProfessorInternalSummaryService service =
                new PeopleProfessorInternalSummaryService(provider(new FailingProfessorInternalSummaryPort()), meterRegistry);

        List<PessoaProfessorInternalSummaryResponse> response =
                service.listarProfessoresPorEscola(UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.professor.internal.summary.reads",
                "operation", "listarProfessoresPorEscola",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    private ObjectProvider<PeopleProfessorInternalSummaryPort> provider(PeopleProfessorInternalSummaryPort port) {
        return new ObjectProvider<>() {
            @Override
            public PeopleProfessorInternalSummaryPort getObject(Object... args) {
                return port;
            }

            @Override
            public PeopleProfessorInternalSummaryPort getIfAvailable() {
                return port;
            }

            @Override
            public PeopleProfessorInternalSummaryPort getIfUnique() {
                return port;
            }

            @Override
            public PeopleProfessorInternalSummaryPort getObject() {
                return port;
            }
        };
    }

    private PessoaProfessorInternalSummaryResponse professor() {
        return new PessoaProfessorInternalSummaryResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Carla Mendes",
                true);
    }

    private static class CountingProfessorInternalSummaryPort implements PeopleProfessorInternalSummaryPort {

        private final PessoaProfessorInternalSummaryResponse professor;
        private int listCalls;

        private CountingProfessorInternalSummaryPort(PessoaProfessorInternalSummaryResponse professor) {
            this.professor = professor;
        }

        @Override
        public Optional<PessoaProfessorInternalSummaryResponse> buscarProfessorPorId(UUID professorId, UUID escolaId) {
            return Optional.of(professor);
        }

        @Override
        public List<PessoaProfessorInternalSummaryResponse> listarProfessoresPorEscola(UUID escolaId) {
            listCalls++;
            return List.of(professor);
        }
    }

    private static class FailingProfessorInternalSummaryPort implements PeopleProfessorInternalSummaryPort {

        @Override
        public Optional<PessoaProfessorInternalSummaryResponse> buscarProfessorPorId(UUID professorId, UUID escolaId) {
            throw new IllegalStateException("professor read failed");
        }

        @Override
        public List<PessoaProfessorInternalSummaryResponse> listarProfessoresPorEscola(UUID escolaId) {
            throw new IllegalStateException("professor read failed");
        }
    }
}
