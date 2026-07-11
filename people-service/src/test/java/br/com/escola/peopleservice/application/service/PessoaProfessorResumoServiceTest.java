package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.dto.PessoaProfessorResumoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaProfessorResumoPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PessoaProfessorResumoServiceTest {

    @Test
    void retornaVazioQuandoGuardBloqueiaLeituraLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaProfessorResumoService service =
                new PessoaProfessorResumoService(
                        provider(null),
                        readRoutingPolicy(false, meterRegistry),
                        meterRegistry);

        Optional<PessoaProfessorResumoResponse> response =
                service.buscarProfessorPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.professor.reads",
                "operation", "buscarProfessorPorId",
                "result", "fallback_guard_blocked").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaProfessorResumoResponse professor = professor();
        CountingProfessorResumoPort port = new CountingProfessorResumoPort(professor);
        PessoaProfessorResumoService service =
                new PessoaProfessorResumoService(
                        provider(port),
                        readRoutingPolicy(true, meterRegistry),
                        meterRegistry);

        List<PessoaProfessorResumoResponse> response =
                service.listarProfessoresPorEscola(UUID.randomUUID());

        assertThat(response).containsExactly(professor);
        assertThat(port.listCalls).isEqualTo(1);
        assertThat(meterRegistry.counter(
                "people.professor.reads",
                "operation", "listarProfessoresPorEscola",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaListaVaziaQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaProfessorResumoService service =
                new PessoaProfessorResumoService(
                        provider(new FailingProfessorResumoPort()),
                        readRoutingPolicy(true, meterRegistry),
                        meterRegistry);

        List<PessoaProfessorResumoResponse> response =
                service.listarProfessoresPorEscola(UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.professor.reads",
                "operation", "listarProfessoresPorEscola",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaVazioQuandoAdapterNaoExisteMesmoComGuardLiberado() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaProfessorResumoService service =
                new PessoaProfessorResumoService(
                        provider(null),
                        readRoutingPolicy(true, meterRegistry),
                        meterRegistry);

        Optional<PessoaProfessorResumoResponse> response =
                service.buscarProfessorPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.professor.reads",
                "operation", "buscarProfessorPorId",
                "result", "fallback_adapter_missing").count()).isEqualTo(1.0d);
    }

    private PeopleReadSourcePolicy readRoutingPolicy(boolean localReadEligible, SimpleMeterRegistry meterRegistry) {
        return new PeopleReadSourcePolicy(
                new br.com.escola.peopleservice.infra.config.PeopleReadModelProperties(
                        localReadEligible,
                        false,
                        localReadEligible,
                        false,
                        localReadEligible,
                        localReadEligible,
                        500,
                        true),
                meterRegistry,
                localReadEligible ? greenState() : new PeopleReadModelSyncState());
    }

    private PeopleReadModelSyncState greenState() {
        PeopleReadModelSyncState state = new PeopleReadModelSyncState();
        state.update(new br.com.escola.peopleservice.application.state.PeopleReadModelSyncSummary(
                true,
                true,
                "completed",
                "local-read-model-backfill-and-reconciliation-completed",
                500,
                8,
                8,
                25,
                25,
                25,
                0,
                false,
                false,
                List.of()));
        return state;
    }

    private ObjectProvider<PessoaProfessorResumoPort> provider(PessoaProfessorResumoPort port) {
        return new ObjectProvider<>() {
            @Override
            public PessoaProfessorResumoPort getObject(Object... args) {
                return port;
            }

            @Override
            public PessoaProfessorResumoPort getIfAvailable() {
                return port;
            }

            @Override
            public PessoaProfessorResumoPort getIfUnique() {
                return port;
            }

            @Override
            public PessoaProfessorResumoPort getObject() {
                return port;
            }
        };
    }

    private PessoaProfessorResumoResponse professor() {
        return new PessoaProfessorResumoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Carla Mendes",
                true);
    }

    private static class CountingProfessorResumoPort implements PessoaProfessorResumoPort {

        private final PessoaProfessorResumoResponse professor;
        private int listCalls;

        private CountingProfessorResumoPort(PessoaProfessorResumoResponse professor) {
            this.professor = professor;
        }

        @Override
        public Optional<PessoaProfessorResumoResponse> buscarProfessorPorId(UUID professorId, UUID escolaId) {
            return Optional.of(professor);
        }

        @Override
        public List<PessoaProfessorResumoResponse> listarProfessoresPorEscola(UUID escolaId) {
            listCalls++;
            return List.of(professor);
        }
    }

    private static class FailingProfessorResumoPort implements PessoaProfessorResumoPort {

        @Override
        public Optional<PessoaProfessorResumoResponse> buscarProfessorPorId(UUID professorId, UUID escolaId) {
            throw new IllegalStateException("professor read failed");
        }

        @Override
        public List<PessoaProfessorResumoResponse> listarProfessoresPorEscola(UUID escolaId) {
            throw new IllegalStateException("professor read failed");
        }
    }
}

