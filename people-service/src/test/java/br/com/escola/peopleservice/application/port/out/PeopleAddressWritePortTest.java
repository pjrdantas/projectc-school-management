package br.com.escola.peopleservice.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoCleanupCommand;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteCommand;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoWriteResult;

class PeopleAddressWritePortTest {

    @Test
    void deveDefinirContratoInternoDeComandoSemPersistenciaLocal() {
        UUID commandId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID tipoEnderecoId = UUID.randomUUID();
        PessoaEnderecoWriteCommand command = new PessoaEnderecoWriteCommand(
                commandId,
                pessoaId,
                escolaId,
                tipoEnderecoId,
                "RESIDENCIAL",
                true,
                "01001000",
                "Praca da Se",
                "100",
                "Apto 10",
                "Se",
                "Sao Paulo",
                "SP",
                "pessoa-endereco:" + pessoaId,
                "phase-71-test");
        PeopleAddressWritePort port = new FakePeopleAddressWritePort();

        PessoaEnderecoWriteResult result = port.criarOuAtualizarEnderecoPrincipal(command);

        assertThat(result.commandId()).isEqualTo(commandId);
        assertThat(result.pessoaId()).isEqualTo(pessoaId);
        assertThat(result.status()).isEqualTo("not_persisted_monolith_contract");
        assertThat(result.selectedSource()).isEqualTo("monolith_proxy");
        assertThat(result.persistedLocally()).isFalse();
        assertThat(result.fallbackRequired()).isTrue();
        assertThat(result.warnings()).contains("people-service-write-monolith-only");
    }

    @Test
    void deveDefinirContratoInternoDeCleanupSemExecutarRemocaoLocal() {
        UUID commandId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        PessoaEnderecoCleanupCommand command = new PessoaEnderecoCleanupCommand(
                commandId,
                pessoaId,
                UUID.randomUUID(),
                true,
                "cleanup:" + pessoaId,
                "phase-71-test");
        PeopleAddressWritePort port = new FakePeopleAddressWritePort();

        PessoaEnderecoWriteResult result = port.removerEnderecosDaPessoa(command);

        assertThat(result.commandId()).isEqualTo(commandId);
        assertThat(result.pessoaId()).isEqualTo(pessoaId);
        assertThat(result.enderecoId()).isNull();
        assertThat(result.pessoaEnderecoId()).isNull();
        assertThat(result.status()).isEqualTo("not_persisted_monolith_contract");
        assertThat(result.persistedLocally()).isFalse();
        assertThat(result.fallbackRequired()).isTrue();
    }

    private static class FakePeopleAddressWritePort implements PeopleAddressWritePort {

        @Override
        public PessoaEnderecoWriteResult criarOuAtualizarEnderecoPrincipal(PessoaEnderecoWriteCommand command) {
            return new PessoaEnderecoWriteResult(
                    command.commandId(),
                    command.pessoaId(),
                    null,
                    null,
                    "not_persisted_monolith_contract",
                    "monolith_proxy",
                    false,
                    true,
                    List.of("people-service-write-monolith-only"));
        }

        @Override
        public PessoaEnderecoWriteResult removerEnderecosDaPessoa(PessoaEnderecoCleanupCommand command) {
            return new PessoaEnderecoWriteResult(
                    command.commandId(),
                    command.pessoaId(),
                    null,
                    null,
                    "not_persisted_monolith_contract",
                    "monolith_proxy",
                    false,
                    true,
                    List.of("people-service-write-monolith-only"));
        }
    }
}

