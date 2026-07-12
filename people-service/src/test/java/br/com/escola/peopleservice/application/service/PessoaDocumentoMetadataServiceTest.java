package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.state.PeopleReadModelSyncSummary;
import br.com.escola.peopleservice.application.dto.PessoaDocumentoMetadataResponse;
import br.com.escola.peopleservice.application.port.out.PessoaDocumentoMetadataPort;
import br.com.escola.peopleservice.infra.config.PeopleReadModelProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PessoaDocumentoMetadataServiceTest {

    @Test
    void retornaVazioQuandoGuardBloqueiaLeitura() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        CountingDocumentoPort port = new CountingDocumentoPort(documento());
        PessoaDocumentoMetadataService service = new PessoaDocumentoMetadataService(
                provider(port),
                guard(new PeopleReadModelSyncState(), meterRegistry),
                meterRegistry);

        Optional<PessoaDocumentoMetadataResponse> response =
                service.buscarDocumentoPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(port.getByIdCalls).isZero();
        assertThat(meterRegistry.counter(
                "people.document.reads",
                "operation", "buscarDocumentoPorId",
                "result", "fallback_guard_blocked").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaVazioQuandoAdapterNaoExiste() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaDocumentoMetadataService service = new PessoaDocumentoMetadataService(
                provider(null),
                guard(greenState(), meterRegistry),
                meterRegistry);

        Optional<PessoaDocumentoMetadataResponse> response =
                service.buscarDocumentoPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.document.reads",
                "operation", "buscarDocumentoPorId",
                "result", "fallback_adapter_missing").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaDocumentoMetadataResponse documento = documento();
        CountingDocumentoPort port = new CountingDocumentoPort(documento);
        PessoaDocumentoMetadataService service = new PessoaDocumentoMetadataService(
                provider(port),
                guard(greenState(), meterRegistry),
                meterRegistry);

        List<PessoaDocumentoMetadataResponse> response =
                service.listarDocumentosPorPessoa(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).containsExactly(documento);
        assertThat(port.listCalls).isEqualTo(1);
        assertThat(meterRegistry.counter(
                "people.document.reads",
                "operation", "listarDocumentosPorPessoa",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void registraFallbackQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaDocumentoMetadataService service = new PessoaDocumentoMetadataService(
                provider(new FailingDocumentoPort()),
                guard(greenState(), meterRegistry),
                meterRegistry);

        List<PessoaDocumentoMetadataResponse> response =
                service.listarDocumentosPorPessoa(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.document.reads",
                "operation", "listarDocumentosPorPessoa",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    private ObjectProvider<PessoaDocumentoMetadataPort> provider(PessoaDocumentoMetadataPort port) {
        return new ObjectProvider<>() {
            @Override
            public PessoaDocumentoMetadataPort getObject(Object... args) {
                return port;
            }

            @Override
            public PessoaDocumentoMetadataPort getIfAvailable() {
                return port;
            }

            @Override
            public PessoaDocumentoMetadataPort getIfUnique() {
                return port;
            }

            @Override
            public PessoaDocumentoMetadataPort getObject() {
                return port;
            }
        };
    }

    private PeopleReadSourcePolicy guard(
            PeopleReadModelSyncState state,
            SimpleMeterRegistry meterRegistry) {
        return new PeopleReadSourcePolicy(
                new PeopleReadModelProperties(true, true, true, false, true, true, 500, true),
                meterRegistry,
                state);
    }

    private PeopleReadModelSyncState greenState() {
        PeopleReadModelSyncState state = new PeopleReadModelSyncState();
        state.update(new PeopleReadModelSyncSummary(
                true,
                true,
                "completed",
                "local-read-model-backfill-and-reconciliation-completed",
                500,
                10,
                10,
                27,
                27,
                27,
                0,
                false,
                false,
                List.of()));
        return state;
    }

    private PessoaDocumentoMetadataResponse documento() {
        return new PessoaDocumentoMetadataResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CPF",
                "CPF",
                "12345678900",
                "/tmp/cpf.pdf",
                "Documento principal",
                OffsetDateTime.parse("2026-01-02T10:15:30Z"));
    }

    private static class CountingDocumentoPort implements PessoaDocumentoMetadataPort {

        private final PessoaDocumentoMetadataResponse documento;
        private int getByIdCalls;
        private int listCalls;

        private CountingDocumentoPort(PessoaDocumentoMetadataResponse documento) {
            this.documento = documento;
        }

        @Override
        public Optional<PessoaDocumentoMetadataResponse> buscarDocumentoPorId(UUID documentoId, UUID escolaId) {
            getByIdCalls++;
            return Optional.of(documento);
        }

        @Override
        public List<PessoaDocumentoMetadataResponse> listarDocumentosPorPessoa(UUID pessoaId, UUID escolaId) {
            listCalls++;
            return List.of(documento);
        }
    }

    private static class FailingDocumentoPort implements PessoaDocumentoMetadataPort {

        @Override
        public Optional<PessoaDocumentoMetadataResponse> buscarDocumentoPorId(UUID documentoId, UUID escolaId) {
            throw new IllegalStateException("document read failed");
        }

        @Override
        public List<PessoaDocumentoMetadataResponse> listarDocumentosPorPessoa(UUID pessoaId, UUID escolaId) {
            throw new IllegalStateException("document read failed");
        }
    }
}


