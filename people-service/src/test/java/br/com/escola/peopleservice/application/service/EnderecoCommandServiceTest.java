package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoCleanupCommand;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteCommand;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class EnderecoCommandServiceTest {

    @Test
    void recebeComandoDeEscritaSemPersistirLocalmente() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        EnderecoCommandService service = new EnderecoCommandService(meterRegistry);
        UUID commandId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();

        var result = service.criarOuAtualizarEnderecoPrincipal(new PessoaEnderecoWriteCommand(
                commandId,
                pessoaId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RESIDENCIAL",
                true,
                "01001000",
                "Praca da Se",
                "100",
                null,
                "Se",
                "Sao Paulo",
                "SP",
                "address-write:" + commandId,
                "address-write-test"));

        assertThat(result.commandId()).isEqualTo(commandId);
        assertThat(result.pessoaId()).isEqualTo(pessoaId);
        assertThat(result.status()).isEqualTo("monolith_write_selected_no_local_persistence");
        assertThat(result.selectedSource()).isEqualTo("monolith_proxy");
        assertThat(result.persistedLocally()).isFalse();
        assertThat(result.fallbackRequired()).isTrue();
        assertThat(result.warnings()).contains(
                "people-service-address-write-monolith-only",
                "monolith-remains-write-authority",
                "local-address-persistence-disabled");
        assertThat(meterRegistry.counter(
                "people.address.write.commands",
                "operation", "criarOuAtualizarEnderecoPrincipal",
                "result", "fallback_required",
                "selectedSource", "monolith_proxy").count()).isEqualTo(1.0d);
    }

    @Test
    void recebeComandoDeCleanupSemRemoverEnderecoLocalmente() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        EnderecoCommandService service = new EnderecoCommandService(meterRegistry);
        UUID commandId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();

        var result = service.removerEnderecosDaPessoa(new PessoaEnderecoCleanupCommand(
                commandId,
                pessoaId,
                UUID.randomUUID(),
                true,
                "address-cleanup:" + commandId,
                "address-cleanup-test"));

        assertThat(result.commandId()).isEqualTo(commandId);
        assertThat(result.pessoaId()).isEqualTo(pessoaId);
        assertThat(result.enderecoId()).isNull();
        assertThat(result.pessoaEnderecoId()).isNull();
        assertThat(result.status()).isEqualTo("monolith_write_selected_no_local_persistence");
        assertThat(result.selectedSource()).isEqualTo("monolith_proxy");
        assertThat(result.persistedLocally()).isFalse();
        assertThat(result.fallbackRequired()).isTrue();
        assertThat(result.warnings()).contains(
                "people-service-address-cleanup-monolith-only",
                "monolith-remains-write-authority",
                "local-address-persistence-disabled");
        assertThat(meterRegistry.counter(
                "people.address.write.commands",
                "operation", "removerEnderecosDaPessoa",
                "result", "fallback_required",
                "selectedSource", "monolith_proxy").count()).isEqualTo(1.0d);
    }

    @Test
    void rejeitaComandoSemIdempotencia() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        EnderecoCommandService service = new EnderecoCommandService(meterRegistry);

        assertThatThrownBy(() -> service.removerEnderecosDaPessoa(new PessoaEnderecoCleanupCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                true,
                " ",
                "address-cleanup-invalid-test")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("idempotencyKey");
        assertThat(meterRegistry.counter(
                "people.address.write.commands",
                "operation", "invalid",
                "result", "rejected",
                "selectedSource", "monolith_proxy").count()).isEqualTo(1.0d);
    }
}



