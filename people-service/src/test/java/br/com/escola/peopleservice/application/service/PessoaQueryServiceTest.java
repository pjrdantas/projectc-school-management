package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport;
import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;
import br.com.escola.peopleservice.application.port.out.PeopleCatalogLocalReadPort;
import br.com.escola.peopleservice.application.port.out.PessoaReadPort;
import br.com.escola.peopleservice.infra.config.PeopleLocalPersistenceProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PessoaQueryServiceTest {

    @Test
    void deveLerCatalogoLocalQuandoGuardaPermite() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        UUID localId = UUID.randomUUID();
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of()),
                new FakeCatalogLocalReadPort(List.of(new PessoaCatalogoResponse(localId, "ALUNO", "Aluno")), List.of(), false),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarTiposPessoa("Bearer token", context());

        assertThat(response).containsExactly(new PessoaCatalogoResponse(localId, "ALUNO", "Aluno"));
        assertThat(monolithCalls).hasValue(0);
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.catalog.reads",
                "operation", "listarTiposPessoa",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoLeituraLocalFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        UUID monolithId = UUID.randomUUID();
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of(new PessoaCatalogoResponse(monolithId, "ALUNO", "Aluno"))),
                new FakeCatalogLocalReadPort(List.of(), List.of(), true),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarTiposPessoa("Bearer token", context());

        assertThat(response).containsExactly(new PessoaCatalogoResponse(monolithId, "ALUNO", "Aluno"));
        assertThat(monolithCalls).hasValue(1);
        assertThat(meterRegistry.counter(
                "people.shadow.local.persistence.catalog.reads",
                "operation", "listarTiposPessoa",
                "result", "fallback").count()).isEqualTo(1.0d);
    }

    private PeopleLocalReadCutoverGuard greenGuard(SimpleMeterRegistry meterRegistry) {
        PeopleLocalPersistenceOperationState state = new PeopleLocalPersistenceOperationState();
        state.update(new PeopleLocalPersistenceOperationReport(
                true,
                true,
                "completed",
                "catalog-backfill-and-reconciliation-completed",
                500,
                2,
                2,
                7,
                7,
                7,
                0,
                false,
                false,
                List.of()));
        return new PeopleLocalReadCutoverGuard(
                new PeopleLocalPersistenceProperties(true, false, true, false, true, true, 500, true),
                meterRegistry,
                state);
    }

    private InternalRequestContext context() {
        return new InternalRequestContext("corr", UUID.randomUUID(), UUID.randomUUID());
    }

    private record FakeCatalogLocalReadPort(
            List<PessoaCatalogoResponse> tiposPessoa,
            List<PessoaCatalogoResponse> tiposEndereco,
            boolean fail) implements PeopleCatalogLocalReadPort {

        @Override
        public List<PessoaCatalogoResponse> listarTiposPessoa() {
            if (fail) {
                throw new IllegalStateException("local-failed");
            }
            return tiposPessoa;
        }

        @Override
        public List<PessoaCatalogoResponse> listarTiposEndereco() {
            if (fail) {
                throw new IllegalStateException("local-failed");
            }
            return tiposEndereco;
        }
    }

    private record FakePessoaReadPort(
            AtomicInteger catalogCalls,
            List<PessoaCatalogoResponse> tiposPessoa) implements PessoaReadPort {

        @Override
        public List<PessoaCatalogoResponse> listarTiposPessoa(String authorization, InternalRequestContext context) {
            catalogCalls.incrementAndGet();
            return tiposPessoa;
        }

        @Override
        public List<PessoaCatalogoResponse> listarTiposEndereco(String authorization, InternalRequestContext context) {
            catalogCalls.incrementAndGet();
            return List.of();
        }

        @Override
        public Optional<PessoaResumoResponse> buscarPessoaPorId(
                String authorization,
                InternalRequestContext context,
                UUID pessoaId) {
            return Optional.empty();
        }

        @Override
        public PessoaConsultaCadastralPageResponse consultarCadastro(
                String authorization,
                InternalRequestContext context,
                String nomeAluno,
                String cpfAluno,
                String nomeResponsavel,
                String cpfResponsavel,
                int page,
                int size) {
            return new PessoaConsultaCadastralPageResponse(List.of(), 0, page, size);
        }
    }
}
