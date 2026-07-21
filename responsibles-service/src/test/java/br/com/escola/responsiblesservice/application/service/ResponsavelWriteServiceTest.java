package br.com.escola.responsiblesservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.dto.CadastrarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.ExclusaoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.AtualizarResponsavelCommand;
import br.com.escola.responsiblesservice.application.dto.ResponsavelReadModelResponse;
import br.com.escola.responsiblesservice.application.exception.DadosResponsavelInvalidosException;
import br.com.escola.responsiblesservice.application.exception.ResponsavelComAlunoVinculadoException;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelWritePort;

class ResponsavelWriteServiceTest {

    @Test
    void deveCriarComCpfNormalizadoEEscolaDoContexto() {
        UUID escolaId = UUID.randomUUID();
        CapturingWritePort port = new CapturingWritePort();
        ResponsavelWriteService service = new ResponsavelWriteService(port);

        service.criar(
                new CadastrarResponsavelCommand(
                        "  Maria da Silva  ", "123.456.789-01", " maria@example.com ", null, null,
                        null, null, null, null, null, null, null),
                new InternalRequestContext("correlation", UUID.randomUUID(), escolaId));

        assertThat(port.escolaId).isEqualTo(escolaId);
        assertThat(port.command.nomeCompleto()).isEqualTo("Maria da Silva");
        assertThat(port.command.cpf()).isEqualTo("12345678901");
        assertThat(port.command.email()).isEqualTo("maria@example.com");
    }

    @Test
    void deveRejeitarCpfInvalido() {
        ResponsavelWriteService service = new ResponsavelWriteService(new CapturingWritePort());

        assertThatThrownBy(() -> service.criar(
                new CadastrarResponsavelCommand("Maria", "123", null, null, null, null, null, null, null, null,
                        null, null),
                new InternalRequestContext("correlation", UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(DadosResponsavelInvalidosException.class)
                .hasMessage("CPF deve conter 11 digitos");
    }

    @Test
    void deveAtualizarComCpfNormalizadoEEscolaDoContexto() {
        UUID responsavelId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        CapturingWritePort port = new CapturingWritePort();
        ResponsavelWriteService service = new ResponsavelWriteService(port);

        service.atualizar(
                responsavelId,
                new AtualizarResponsavelCommand(
                        "  Maria Atualizada ", "123.456.789-01", null, null, null, null, null, null, null, null,
                        null, null),
                new InternalRequestContext("correlation", UUID.randomUUID(), escolaId));

        assertThat(port.responsavelId).isEqualTo(responsavelId);
        assertThat(port.escolaId).isEqualTo(escolaId);
        assertThat(port.updatedCommand.nomeCompleto()).isEqualTo("Maria Atualizada");
        assertThat(port.updatedCommand.cpf()).isEqualTo("12345678901");
    }

    @Test
    void deveBloquearExclusaoQuandoResponsavelPossuiAlunoVinculado() {
        CapturingWritePort port = new CapturingWritePort();
        port.exclusaoResultado = ExclusaoResponsavelResultado.POSSUI_ALUNO_VINCULADO;
        ResponsavelWriteService service = new ResponsavelWriteService(port);

        assertThatThrownBy(() -> service.excluir(
                UUID.randomUUID(), new InternalRequestContext("correlation", UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(ResponsavelComAlunoVinculadoException.class);
    }

    private static class CapturingWritePort implements ResponsavelWritePort {
        private CadastrarResponsavelCommand command;
        private AtualizarResponsavelCommand updatedCommand;
        private UUID responsavelId;
        private ExclusaoResponsavelResultado exclusaoResultado = ExclusaoResponsavelResultado.EXCLUIDO;
        private UUID escolaId;

        @Override
        public ResponsavelReadModelResponse criar(CadastrarResponsavelCommand command, UUID escolaId) {
            this.command = command;
            this.escolaId = escolaId;
            return new ResponsavelReadModelResponse(
                    UUID.randomUUID(), command.nomeCompleto(), command.cpf(), command.email(), command.telefone(),
                    command.rg(), command.cep(), command.logradouro(), command.numero(), command.complemento(),
                    command.bairro(), command.cidade(), command.uf(), escolaId, null, LocalDateTime.now());
        }

        @Override
        public java.util.Optional<ResponsavelReadModelResponse> atualizar(
                UUID responsavelId,
                AtualizarResponsavelCommand command,
                UUID escolaId) {
            this.responsavelId = responsavelId;
            this.updatedCommand = command;
            this.escolaId = escolaId;
            return java.util.Optional.of(new ResponsavelReadModelResponse(
                    responsavelId, command.nomeCompleto(), command.cpf(), command.email(), command.telefone(),
                    command.rg(), command.cep(), command.logradouro(), command.numero(), command.complemento(),
                    command.bairro(), command.cidade(), command.uf(), escolaId, null, LocalDateTime.now()));
        }

        @Override
        public ExclusaoResponsavelResultado excluir(UUID responsavelId, UUID escolaId) {
            this.responsavelId = responsavelId;
            this.escolaId = escolaId;
            return exclusaoResultado;
        }
    }
}
