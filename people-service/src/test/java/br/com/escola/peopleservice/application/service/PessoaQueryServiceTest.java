package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.state.PeopleReadModelSyncSummary;
import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaCatalogoPort;
import br.com.escola.peopleservice.application.port.out.AlunoResponsavelPort;
import br.com.escola.peopleservice.application.port.out.PessoaReadPort;
import br.com.escola.peopleservice.infra.config.PeopleReadModelProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PessoaQueryServiceTest {

    @Test
    void deveLerCatalogoLocalQuandoGuardaPermite() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        UUID localId = UUID.randomUUID();
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of()),
                new FakePessoaCatalogoPort(List.of(new PessoaCatalogoResponse(localId, "ALUNO", "Aluno")), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarTiposPessoa("Bearer token", context());

        assertThat(response).containsExactly(new PessoaCatalogoResponse(localId, "ALUNO", "Aluno"));
        assertThat(monolithCalls).hasValue(0);
        assertThat(meterRegistry.counter(
                "people.catalog.reads",
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
                new FakePessoaCatalogoPort(List.of(), List.of(), true),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarTiposPessoa("Bearer token", context());

        assertThat(response).containsExactly(new PessoaCatalogoResponse(monolithId, "ALUNO", "Aluno"));
        assertThat(monolithCalls).hasValue(1);
        assertThat(meterRegistry.counter(
                "people.catalog.reads",
                "operation", "listarTiposPessoa",
                "result", "fallback").count()).isEqualTo(1.0d);
    }

    @Test
    void deveLerPessoaPorIdDoReadModelLocalQuandoGuardaPermite() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        UUID pessoaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        PessoaResumoResponse local = new PessoaResumoResponse(pessoaId, "Pessoa Local", escolaId, null, true);
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.of(local), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.buscarPessoaPorId("Bearer token", context(escolaId), pessoaId);

        assertThat(response).isEqualTo(local);
        assertThat(monolithCalls).hasValue(0);
        assertThat(meterRegistry.counter(
                "people.identity.reads",
                "operation", "buscarPorId",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoPessoaLocalNaoExiste() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        UUID pessoaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        PessoaResumoResponse monolith = new PessoaResumoResponse(pessoaId, "Pessoa Monolito", escolaId, "Escola", true);
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of(), Optional.of(monolith)),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.buscarPessoaPorId("Bearer token", context(escolaId), pessoaId);

        assertThat(response).isEqualTo(monolith);
        assertThat(monolithCalls).hasValue(1);
        assertThat(meterRegistry.counter(
                "people.identity.reads",
                "operation", "buscarPorId",
                "result", "fallback_not_found").count()).isEqualTo(1.0d);
    }

    @Test
    void deveConsultarCadastroNoReadModelLocalQuandoGuardaPermite() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        var localPage = new PessoaConsultaCadastralPageResponse(List.of(), 3, 1, 15);
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(localPage, false),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.consultarCadastro(
                "Bearer token",
                context(),
                "Ana",
                null,
                "Rita",
                null,
                1,
                15);

        assertThat(response).isEqualTo(localPage);
        assertThat(monolithCalls).hasValue(0);
        assertThat(meterRegistry.counter(
                "people.studentresponsible.reads",
                "operation", "consultarCadastro",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoConsultaCadastroLocalFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        var monolithPage = new PessoaConsultaCadastralPageResponse(List.of(), 1, 0, 20);
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of(), Optional.empty(), monolithPage),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), true),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.consultarCadastro(
                "Bearer token",
                context(),
                null,
                null,
                null,
                null,
                0,
                20);

        assertThat(response).isEqualTo(monolithPage);
        assertThat(monolithCalls).hasValue(1);
        assertThat(meterRegistry.counter(
                "people.studentresponsible.reads",
                "operation", "consultarCadastro",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    private PeopleReadSourcePolicy greenGuard(SimpleMeterRegistry meterRegistry) {
        PeopleReadModelSyncState state = new PeopleReadModelSyncState();
        state.update(new PeopleReadModelSyncSummary(
                true,
                true,
                "completed",
                "local-read-model-backfill-and-reconciliation-completed",
                500,
                4,
                4,
                12,
                12,
                12,
                0,
                false,
                false,
                List.of()));
        return new PeopleReadSourcePolicy(
                new PeopleReadModelProperties(true, false, true, false, true, true, 500, true),
                meterRegistry,
                state);
    }

    private InternalRequestContext context() {
        return context(UUID.randomUUID());
    }

    private InternalRequestContext context(UUID escolaId) {
        return new InternalRequestContext("corr", UUID.randomUUID(), escolaId);
    }

    private record FakePessoaCatalogoPort(
            List<PessoaCatalogoResponse> tiposPessoa,
            List<PessoaCatalogoResponse> tiposEndereco,
            boolean fail) implements PessoaCatalogoPort {

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

        @Override
        public List<PessoaCatalogoResponse> listarStatusAluno() {
            if (fail) {
                throw new IllegalStateException("local-failed");
            }
            return List.of();
        }

        @Override
        public List<PessoaCatalogoResponse> listarParentescos() {
            if (fail) {
                throw new IllegalStateException("local-failed");
            }
            return List.of();
        }
    }

    private record FakePessoaPort(
            Optional<PessoaResumoResponse> response,
            boolean fail) implements br.com.escola.peopleservice.application.port.out.PessoaPort {

        @Override
        public Optional<PessoaResumoResponse> buscarPessoaPorId(UUID pessoaId, UUID escolaId) {
            if (fail) {
                throw new IllegalStateException("identity-local-failed");
            }
            return response;
        }
    }

    private record FakeAlunoResponsavelPort(
            PessoaConsultaCadastralPageResponse response,
            boolean fail) implements AlunoResponsavelPort {

        @Override
        public PessoaConsultaCadastralPageResponse consultarCadastro(
                String nomeAluno,
                String cpfAluno,
                String nomeResponsavel,
                String cpfResponsavel,
                int page,
                int size) {
            if (fail) {
                throw new IllegalStateException("student-responsible-local-failed");
            }
            return response;
        }
    }

    private record FakePessoaReadPort(
            AtomicInteger catalogCalls,
            List<PessoaCatalogoResponse> tiposPessoa,
            Optional<PessoaResumoResponse> pessoa,
            PessoaConsultaCadastralPageResponse consultaCadastroResponse) implements PessoaReadPort {

        private FakePessoaReadPort(AtomicInteger catalogCalls, List<PessoaCatalogoResponse> tiposPessoa) {
            this(catalogCalls, tiposPessoa, Optional.empty(), new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20));
        }

        private FakePessoaReadPort(
                AtomicInteger catalogCalls,
                List<PessoaCatalogoResponse> tiposPessoa,
                Optional<PessoaResumoResponse> pessoa) {
            this(catalogCalls, tiposPessoa, pessoa, new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20));
        }

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
            catalogCalls.incrementAndGet();
            return pessoa;
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
            catalogCalls.incrementAndGet();
            return consultaCadastroResponse;
        }
    }
}


