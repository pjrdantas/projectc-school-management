package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport;
import br.com.escola.peopleservice.application.dto.PessoaDocumentoMetadataLocalReadResponse;
import br.com.escola.peopleservice.application.port.out.PeopleDocumentMetadataLocalReadPort;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PeopleDocumentMetadataLocalReadServiceTest {

    @Test
    void retornaVazioQuandoGuardBloqueiaLeitura() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        CountingDocumentLocalReadPort port = new CountingDocumentLocalReadPort(documento());
        PeopleDocumentMetadataLocalReadService service = new PeopleDocumentMetadataLocalReadService(
                provider(port),
                guard(new PeopleLocalPersistenceOperationState(), meterRegistry),
                meterRegistry);

        Optional<PessoaDocumentoMetadataLocalReadResponse> response =
                service.buscarDocumentoPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(port.getByIdCalls).isZero();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.document.metadata.reads",
                "operation", "buscarDocumentoPorId",
                "result", "fallback_guard_blocked").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaVazioQuandoAdapterNaoExiste() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleDocumentMetadataLocalReadService service = new PeopleDocumentMetadataLocalReadService(
                provider(null),
                guard(greenState(), meterRegistry),
                meterRegistry);

        Optional<PessoaDocumentoMetadataLocalReadResponse> response =
                service.buscarDocumentoPorId(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.document.metadata.reads",
                "operation", "buscarDocumentoPorId",
                "result", "fallback_adapter_missing").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoDisponivel() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaDocumentoMetadataLocalReadResponse documento = documento();
        CountingDocumentLocalReadPort port = new CountingDocumentLocalReadPort(documento);
        PeopleDocumentMetadataLocalReadService service = new PeopleDocumentMetadataLocalReadService(
                provider(port),
                guard(greenState(), meterRegistry),
                meterRegistry);

        List<PessoaDocumentoMetadataLocalReadResponse> response =
                service.listarDocumentosPorPessoa(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).containsExactly(documento);
        assertThat(port.listCalls).isEqualTo(1);
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.document.metadata.reads",
                "operation", "listarDocumentosPorPessoa",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void registraFallbackQuandoAdapterFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PeopleDocumentMetadataLocalReadService service = new PeopleDocumentMetadataLocalReadService(
                provider(new FailingDocumentLocalReadPort()),
                guard(greenState(), meterRegistry),
                meterRegistry);

        List<PessoaDocumentoMetadataLocalReadResponse> response =
                service.listarDocumentosPorPessoa(UUID.randomUUID(), UUID.randomUUID());

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.document.metadata.reads",
                "operation", "listarDocumentosPorPessoa",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    private ObjectProvider<PeopleDocumentMetadataLocalReadPort> provider(PeopleDocumentMetadataLocalReadPort port) {
        return new ObjectProvider<>() {
            @Override
            public PeopleDocumentMetadataLocalReadPort getObject(Object... args) {
                return port;
            }

            @Override
            public PeopleDocumentMetadataLocalReadPort getIfAvailable() {
                return port;
            }

            @Override
            public PeopleDocumentMetadataLocalReadPort getIfUnique() {
                return port;
            }

            @Override
            public PeopleDocumentMetadataLocalReadPort getObject() {
                return port;
            }
        };
    }

    private PeopleLocalReadCutoverGuard guard(
            PeopleLocalPersistenceOperationState state,
            SimpleMeterRegistry meterRegistry) {
        return new PeopleLocalReadCutoverGuard(
                new PeopleLocalPersistenceProperties(true, true, true, false, true, true, 500, true),
                meterRegistry,
                state);
    }

    private PeopleLocalPersistenceOperationState greenState() {
        PeopleLocalPersistenceOperationState state = new PeopleLocalPersistenceOperationState();
        state.update(new PeopleLocalPersistenceOperationReport(
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

    private PessoaDocumentoMetadataLocalReadResponse documento() {
        return new PessoaDocumentoMetadataLocalReadResponse(
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

    private static class CountingDocumentLocalReadPort implements PeopleDocumentMetadataLocalReadPort {

        private final PessoaDocumentoMetadataLocalReadResponse documento;
        private int getByIdCalls;
        private int listCalls;

        private CountingDocumentLocalReadPort(PessoaDocumentoMetadataLocalReadResponse documento) {
            this.documento = documento;
        }

        @Override
        public Optional<PessoaDocumentoMetadataLocalReadResponse> buscarDocumentoPorId(UUID documentoId, UUID escolaId) {
            getByIdCalls++;
            return Optional.of(documento);
        }

        @Override
        public List<PessoaDocumentoMetadataLocalReadResponse> listarDocumentosPorPessoa(UUID pessoaId, UUID escolaId) {
            listCalls++;
            return List.of(documento);
        }
    }

    private static class FailingDocumentLocalReadPort implements PeopleDocumentMetadataLocalReadPort {

        @Override
        public Optional<PessoaDocumentoMetadataLocalReadResponse> buscarDocumentoPorId(UUID documentoId, UUID escolaId) {
            throw new IllegalStateException("document read failed");
        }

        @Override
        public List<PessoaDocumentoMetadataLocalReadResponse> listarDocumentosPorPessoa(UUID pessoaId, UUID escolaId) {
            throw new IllegalStateException("document read failed");
        }
    }
}
