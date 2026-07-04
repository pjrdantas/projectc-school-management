package br.com.escola.peopleservice.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.dto.PessoaEnderecoLocalReadResponse;

class PeopleAddressLocalReadPortTest {

    @Test
    void deveDefinirContratoInternoSemRotaOuAdapterOperacional() {
        UUID pessoaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID pessoaEnderecoId = UUID.randomUUID();
        UUID enderecoId = UUID.randomUUID();
        UUID tipoEnderecoId = UUID.randomUUID();
        PessoaEnderecoLocalReadResponse endereco = new PessoaEnderecoLocalReadResponse(
                pessoaEnderecoId,
                pessoaId,
                enderecoId,
                tipoEnderecoId,
                "RESIDENCIAL",
                "Residencial",
                true,
                "01001000",
                "Praca da Se",
                "100",
                "Apto 10",
                "Se",
                "Sao Paulo",
                "SP");

        PeopleAddressLocalReadPort port = new FakePeopleAddressLocalReadPort(endereco);

        assertThat(port.buscarEnderecoPrincipalPorPessoa(pessoaId, escolaId))
                .contains(endereco);
        assertThat(port.listarEnderecosPorPessoa(pessoaId, escolaId))
                .containsExactly(endereco);
    }

    private record FakePeopleAddressLocalReadPort(
            PessoaEnderecoLocalReadResponse endereco) implements PeopleAddressLocalReadPort {

        @Override
        public Optional<PessoaEnderecoLocalReadResponse> buscarEnderecoPrincipalPorPessoa(UUID pessoaId, UUID escolaId) {
            return Optional.of(endereco);
        }

        @Override
        public List<PessoaEnderecoLocalReadResponse> listarEnderecosPorPessoa(UUID pessoaId, UUID escolaId) {
            return List.of(endereco);
        }
    }
}
