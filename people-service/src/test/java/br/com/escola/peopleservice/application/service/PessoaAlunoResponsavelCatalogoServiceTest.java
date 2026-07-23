package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaCatalogoPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PessoaAlunoResponsavelCatalogoServiceTest {

    @Test
    void retornaListaVaziaQuandoGuardBloqueiaStatusAluno() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaAlunoResponsavelCatalogoService service = new PessoaAlunoResponsavelCatalogoService(
                provider(null),
                readRoutingPolicy(false, meterRegistry),
                meterRegistry);

        var response = service.listarStatusAluno();

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.catalog.reads",
                "operation", "listarStatusAluno",
                "result", "fallback_guard_blocked").count()).isEqualTo(1.0d);
    }

    @Test
    void consultaAdapterQuandoParentescosEstaoDisponiveis() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        var parentesco = new PessoaCatalogoResponse(UUID.randomUUID(), "MAE", "Mae");
        PessoaAlunoResponsavelCatalogoService service = new PessoaAlunoResponsavelCatalogoService(
                provider(new FakeCatalogoPort(List.of(), List.of(parentesco), false)),
                readRoutingPolicy(true, meterRegistry),
                meterRegistry);

        var response = service.listarParentescos();

        assertThat(response).containsExactly(parentesco);
        assertThat(meterRegistry.counter(
                "people.catalog.reads",
                "operation", "listarParentescos",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaListaVaziaQuandoAdapterFalhaAoLerStatusAluno() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaAlunoResponsavelCatalogoService service = new PessoaAlunoResponsavelCatalogoService(
                provider(new FakeCatalogoPort(List.of(), List.of(), true)),
                readRoutingPolicy(true, meterRegistry),
                meterRegistry);

        var response = service.listarStatusAluno();

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.catalog.reads",
                "operation", "listarStatusAluno",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    @Test
    void retornaListaVaziaQuandoAdapterNaoExisteMesmoComGuardLiberado() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaAlunoResponsavelCatalogoService service = new PessoaAlunoResponsavelCatalogoService(
                provider(null),
                readRoutingPolicy(true, meterRegistry),
                meterRegistry);

        var response = service.listarParentescos();

        assertThat(response).isEmpty();
        assertThat(meterRegistry.counter(
                "people.catalog.reads",
                "operation", "listarParentescos",
                "result", "fallback_adapter_missing").count()).isEqualTo(1.0d);
    }

    private DataAccessPolicy readRoutingPolicy(boolean localReadEligible, SimpleMeterRegistry meterRegistry) {
        return new DataAccessPolicy(
                new br.com.escola.peopleservice.infra.config.RuntimeProperties(
                        localReadEligible,
                        localReadEligible,
                        false),
                meterRegistry);
    }

    private ObjectProvider<PessoaCatalogoPort> provider(PessoaCatalogoPort port) {
        return new ObjectProvider<>() {
            @Override
            public PessoaCatalogoPort getObject(Object... args) {
                return port;
            }

            @Override
            public PessoaCatalogoPort getIfAvailable() {
                return port;
            }

            @Override
            public PessoaCatalogoPort getIfUnique() {
                return port;
            }

            @Override
            public PessoaCatalogoPort getObject() {
                return port;
            }
        };
    }

    private record FakeCatalogoPort(
            List<PessoaCatalogoResponse> statusAluno,
            List<PessoaCatalogoResponse> parentescos,
            boolean fail) implements PessoaCatalogoPort {

        @Override
        public List<PessoaCatalogoResponse> listarTiposPessoa() {
            return List.of();
        }

        @Override
        public List<PessoaCatalogoResponse> listarTiposEndereco() {
            return List.of();
        }

        @Override
        public List<PessoaCatalogoResponse> listarStatusAluno() {
            if (fail) {
                throw new IllegalStateException("catalog-read-failed");
            }
            return statusAluno;
        }

        @Override
        public List<PessoaCatalogoResponse> listarParentescos() {
            if (fail) {
                throw new IllegalStateException("catalog-read-failed");
            }
            return parentescos;
        }
    }
}

